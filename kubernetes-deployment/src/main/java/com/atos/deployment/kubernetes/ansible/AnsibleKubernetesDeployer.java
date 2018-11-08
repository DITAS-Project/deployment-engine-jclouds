package com.atos.deployment.kubernetes.ansible;

import com.atos.deployment.beans.InfrastructureDeploymentInfo;
import com.atos.deployment.beans.NodeInfo;
import com.atos.deployment.kubernetes.KubernetesDeployer;
import com.atos.deployment.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class AnsibleKubernetesDeployer implements KubernetesDeployer {

  private static final Logger logger = LoggerFactory.getLogger(AnsibleKubernetesDeployer.class);

  private String scriptsPath;
  private String storagePath;

  public AnsibleKubernetesDeployer(String scriptsPath, String storagePath) {
    this.scriptsPath = scriptsPath;
    this.storagePath = storagePath;
  }

  @Override
  public boolean deployKubernetesCluster(InfrastructureDeploymentInfo infra) {
    try {
      addToHostFile(infra);
      deployKubernetes(infra);
      return true;
    } catch (Exception e) {
      logger.error("Deploying Kubernetes on infrastructure " + infra.getInfraId());
    }

    return false;
  }

  private void deployKubernetes(InfrastructureDeploymentInfo infra)
      throws IOException, InterruptedException {
    String inventory = createInventory(infra);
    if (inventory != null) {
      Map<String, String> vars = new HashMap<>();
      vars.put("masterUsername", infra.getMaster().getUsername());
      executePlaybook("ansible_deploy.yml", inventory, vars);
    }
  }

  private String createInventory(InfrastructureDeploymentInfo infra) throws IOException {
    File inventoryDir = new File(storagePath + File.separator + infra.getInfraId());
    logger.trace("Creating inventory file in " + inventoryDir.getPath());
    boolean success = inventoryDir.mkdirs();
    if (success) {
      File inventoryFile = new File(inventoryDir.getPath() + File.separator + "inventory");
      logger.trace("Writing inventory file " + inventoryFile.getPath());
      FileWriter writer = null;
      try {
        writer = new FileWriter(inventoryFile);
        writer.write("[master]\n");
        writer.write(getHostLine(infra.getMaster()));
        writer.write("[slaves]\n");
        for (NodeInfo node : infra.getSlaves()) {
          writer.write(getHostLine(node));
        }
        writer.close();

        logger.trace("Inventory file complete");

        return inventoryFile.getAbsolutePath();

      } catch (IOException e) {
        logger.error("Error writing inventory file for infrastructure " + infra.getInfraId(), e);
        throw e;
      } finally {
        if (writer != null) {
          writer.close();
        }
      }
    }

    return null;
  }

  private String getHostLine(NodeInfo node) {
    return node.getName()
        + "  ansible_host="
        + node.getIp()
        + " ansible_user="
        + node.getUsername()
        + "\n";
  }

  private void addToHostFile(InfrastructureDeploymentInfo infra)
      throws InterruptedException, TimeoutException, IOException {

    logger.trace("Adding master to known hosts");

    addHostToHostFile(infra.getMaster());

    logger.trace("Master added to known hosts. Adding slaves");

    for (NodeInfo node : infra.getSlaves()) {
      addHostToHostFile(node);
    }

    logger.trace("Slaves added to known hosts");
  }

  private void addHostToHostFile(NodeInfo hostInfo)
      throws IOException, InterruptedException, TimeoutException {

    logger.trace("Adding host " + hostInfo.getName() + " to known hosts");

    clearKnownHost(hostInfo.getIp());

    String host = hostInfo.getUsername() + "@" + hostInfo.getIp();
    String command =
        "echo "
            + hostInfo.getIp()
            + " "
            + hostInfo.getName()
            + " | sudo tee -a /etc/hosts > /dev/null 2>&1";

    List<String> sshCommand = Arrays.asList("ssh", "-o", "StrictHostKeyChecking=no", host, command);

    try {
      Utils.retry(
          status -> !status.equals("starting"),
          60000,
          10000,
          () -> {
            try {
              int result = Utils.executeProcess(sshCommand, System.getProperty("user.home"));
            } catch (Exception e) {
              return "starting";
            }
            return "started";
          });
    } catch (TimeoutException e) {
      logger.error("Error waiting for ssh service to be ready", e);
      throw e;
    }

    logger.trace("Host " + hostInfo.getName() + " added to known hosts");
  }

  private void clearKnownHost(String ip) throws IOException, InterruptedException {
    logger.trace("Clearing host " + ip + " key");
    Map<String, String> vars = new HashMap<>();
    vars.put("host_ip", ip);
    executePlaybook("clear_known_hosts.yml", null, vars);
    logger.trace("Known hosts clear");
  }

  private int executePlaybook(
      String playbookName, String inventoryPath, Map<String, String> extraVars)
      throws IOException, InterruptedException {

    List<String> command = new ArrayList<>();
    command.add("ansible-playbook");
    command.add(playbookName);
    if (inventoryPath != null) {
      command.add("--inventory=" + inventoryPath);
    }

    if (extraVars != null && !extraVars.isEmpty()) {
      command.add("--extra-vars");
      command.add(
          extraVars
              .entrySet()
              .stream()
              .map(entry -> entry.getKey() + "=" + entry.getValue())
              .collect(Collectors.joining(" ")));
    }

    return Utils.executeProcess(command, scriptsPath);
  }
}
