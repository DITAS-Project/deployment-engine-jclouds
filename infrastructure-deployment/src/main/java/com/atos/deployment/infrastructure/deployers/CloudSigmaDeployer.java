package com.atos.deployment.infrastructure.deployers;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

import com.atos.deployment.beans.Infrastructure;
import com.atos.deployment.beans.InfrastructureDeploymentInfo;
import com.atos.deployment.beans.NodeInfo;
import com.atos.deployment.beans.Resource;
import com.atos.deployment.beans.RoleType;
import com.atos.deployment.beans.ServerStatusType;
import com.atos.deployment.utils.Utils;
import com.sun.jna.platform.win32.OaIdl;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jclouds.ContextBuilder;
import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.domain.DeviceEmulationType;
import org.jclouds.cloudsigma2.domain.DriveInfo;
import org.jclouds.cloudsigma2.domain.DriveStatus;
import org.jclouds.cloudsigma2.domain.IPConfiguration;
import org.jclouds.cloudsigma2.domain.IPConfigurationType;
import org.jclouds.cloudsigma2.domain.IPInfo;
import org.jclouds.cloudsigma2.domain.LibraryDrive;
import org.jclouds.cloudsigma2.domain.MediaType;
import org.jclouds.cloudsigma2.domain.NIC;
import org.jclouds.cloudsigma2.domain.ServerDrive;
import org.jclouds.cloudsigma2.domain.ServerInfo;
import org.jclouds.cloudsigma2.domain.ServerStatus;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class CloudSigmaDeployer implements Deployer {

    private static final String ENDPOINT = "api_endpoint";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final Logger logger = LoggerFactory.getLogger(CloudSigmaDeployer.class);

    ComputeService client;
    ComputeServiceContext context;
    CloudSigma2Api cloudSigmaApi;

    public CloudSigmaDeployer() throws ConfigurationException {
        Configurations configs = new Configurations();

        String configFile = System.getProperty("user.home") + File.separator + ".cloudsigma.conf";
        PropertiesConfiguration cloudsigmaConfig = configs.properties(new File(configFile));

        context = ContextBuilder.newBuilder("cloudsigma2")
                .endpoint(cloudsigmaConfig.getString(ENDPOINT))
                .credentials(cloudsigmaConfig.getString(USERNAME), cloudsigmaConfig.getString(PASSWORD))
                .modules(ImmutableSet.<Module> of(new SLF4JLoggingModule(),
                        new SshjSshClientModule()))
                .buildView(ComputeServiceContext.class);

        this.client = context.getComputeService();
        cloudSigmaApi = context.unwrapApi(CloudSigma2Api.class);
    }

    @Override
    public InfrastructureDeploymentInfo deploy(Infrastructure infrastructure) {
        InfrastructureDeploymentInfo info = new InfrastructureDeploymentInfo();

        info.setMaster(deployVM(infrastructure.getResources().stream()
                .filter(resource -> RoleType.MASTER.equals(resource.getRole()))
                .findFirst().get(), infrastructure.getName()));

        info.setSlaves(infrastructure.getResources().stream()
                .filter(resource -> RoleType.SLAVE.equals(resource.getRole()))
                .map(resource -> deployVM(resource, infrastructure.getName()))
                .collect(Collectors.toList()));

        return info;
    }

    private NodeInfo deployVM(Resource resource, String infraName) {

        String hostname = infraName + "-" + resource.getName();

        NodeInfo result = new NodeInfo();
        result.setDriveUuids(new ArrayList<>());
        result.setRole(resource.getRole().toString());
        result.setStatus(ServerStatusType.CREATING);


        LibraryDrive options = new LibraryDrive.Builder()
                .media(MediaType.DISK)
                .size(BigInteger.valueOf(resource.getDisk() * 1024 * 1024)).build();
// Next step is to clone the drive and identify it in our drive list
        LibraryDrive cloned = cloudSigmaApi.cloneLibraryDrive(resource.getImageId(), options);

        result.getDriveUuids().add(cloned.getUuid());

        if (resource.getDrives() != null) {
            for (int i = 0; i < resource.getDrives().size(); i++) {
                DriveInfo dataDrive = new DriveInfo.Builder()
                        .media(MediaType.DISK)
                        .name(hostname + "-data-" + i)
                        .size(BigInteger.valueOf(resource.getDrives().get(i).getSize() * 1024 * 1024))
                        .build();

                dataDrive = cloudSigmaApi.createDrive(dataDrive);
                result.getDriveUuids().add(dataDrive.getUuid());
            }
        }

        try {

            List<ServerDrive> drives = new ArrayList<>();

            for (int i=0 ; i< result.getDriveUuids().size(); i++) {
                drives.add(waitForDriveReady(60000, result.getDriveUuids().get(i))
                        .toServerDrive(i+1, "0:" + (i+1), DeviceEmulationType.VIRTIO));
            }

            NIC nic = new NIC.Builder().ipV4Configuration(new IPConfiguration.Builder()
                    .configurationType(IPConfigurationType.DHCP).build()).build();

            Optional<IPInfo> ip = cloudSigmaApi.listIPInfo().concat().firstMatch(ipInfo -> ipInfo.getServer() == null);
            if (ip.isPresent()) {
                nic = ip.get().toNIC();
            }

            ServerInfo serverToCreate = new ServerInfo.Builder()
                    .name(hostname)
                    .memory(BigInteger.valueOf(resource.getRam() * 1024 * 1024))
                    .cpu(resource.getCpu())
                    .vncPassword("new_password")
                    .nics(ImmutableList.of(nic))
                    .drives(drives)
                    .build();

            ServerInfo createdServer = cloudSigmaApi.createServer(serverToCreate);

            cloudSigmaApi.startServer(createdServer.getUuid());

            ServerInfo server = Utils.retry(
                    serverInfo -> ServerStatus.RUNNING.equals(((ServerInfo)serverInfo).getStatus()),
                    60000,
                    10000,
                    () -> cloudSigmaApi.getServerInfo(createdServer.getUuid()));

            result.setName(server.getName());
            result.setIp(server.getNics().get(0).getIpV4Configuration().getIp().getUuid());
            result.setUsername("cloudsigma");
            result.setUuid(server.getUuid());

        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;

    }

    public DriveInfo waitForDriveReady(long timeout, String uuid) throws TimeoutException, InterruptedException {
        return Utils.retry(
                driveInfo -> DriveStatus.UNMOUNTED.equals(driveInfo.getStatus()),
                timeout,
                10000,
                () -> cloudSigmaApi.getDriveInfo(uuid));
    }

    @Override
    public InfrastructureDeploymentInfo delete(InfrastructureDeploymentInfo infrastructure) {
        List<NodeInfo> toDelete = new ArrayList<>();
        for (NodeInfo node : infrastructure.getSlaves()) {
            try {
                deleteNode(node);
                toDelete.add(node);
            } catch (Exception e) {
                logger.info("Error deleting slave node " + node.getName() + " from infrastructure " + infrastructure.getInfraId());
            }
        }
        infrastructure.getSlaves().removeAll(toDelete);
        try {
            deleteNode(infrastructure.getMaster());
            infrastructure.setMaster(null);
        } catch (Exception e) {
            logger.info("Error deleting master node from infrastructure " + infrastructure.getInfraId());
        }
        return infrastructure;
    }

    private void deleteNode(NodeInfo node) throws TimeoutException, InterruptedException {
        cloudSigmaApi.stopServer(node.getUuid());
        Utils.retry(status -> !ServerStatus.STOPPING.equals(status), 60000, 10000, () -> cloudSigmaApi.getServerInfo(node.getUuid()).getStatus());
        cloudSigmaApi.deleteServer(node.getUuid());
        cloudSigmaApi.deleteDrives(node.getDriveUuids());
    }
}
