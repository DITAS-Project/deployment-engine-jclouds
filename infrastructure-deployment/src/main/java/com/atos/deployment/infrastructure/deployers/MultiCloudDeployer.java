package com.atos.deployment.infrastructure.deployers;

import com.atos.deployment.beans.CloudProviderInfo;
import com.atos.deployment.beans.Deployment;
import com.atos.deployment.beans.DeploymentInfo;
import com.atos.deployment.beans.Infrastructure;
import com.atos.deployment.beans.InfrastructureDeploymentInfo;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MultiCloudDeployer {

  private static final Logger logger = LoggerFactory.getLogger(MultiCloudDeployer.class);

  private static Deployer getDeployer(CloudProviderInfo providerInfo)
      throws ConfigurationException {
    switch (providerInfo.getApiType()) {
      case CLOUDSIGMA:
        return new CloudSigmaDeployer();
    }

    return null;
  }

  public DeploymentInfo createDeployment(Deployment dep) {

    DeploymentInfo result = new DeploymentInfo();
    result.setId(UUID.randomUUID().toString());
    result.setInfrastructures(new ArrayList<>());

    for (Infrastructure infra : dep.getInfrastructure()) {
      InfrastructureDeploymentInfo infraResult = null;

      try {
        Deployer deployer = getDeployer(infra.getProvider());
        infraResult = deployer.deploy(infra);
      } catch (ConfigurationException e) {
        logger.error("Error getting deployer for provider " + infra.getProvider().getApiType(), e);
      }

      if (infraResult != null) {
        infraResult.setInfraId(UUID.randomUUID().toString());
        infraResult.setProvider(infra.getProvider());
        result.getInfrastructures().add(infraResult);
      }
    }
    return result;
  }

  public DeploymentInfo deleteInfrastructure(DeploymentInfo deployment, String infraId) {

    if (deployment != null) {
      List<InfrastructureDeploymentInfo> toDelete = new ArrayList<>();
      if (infraId != null) {
        InfrastructureDeploymentInfo infra = deployment.getInfrastructure(infraId);
        if (infra != null) {
          toDelete.add(infra);
        }
      } else {
        toDelete.addAll(deployment.getInfrastructures());
      }

      List<InfrastructureDeploymentInfo> deleted = deleteInfrastructures(toDelete);
      deployment.getInfrastructures().removeAll(deleted);
    }

    return deployment;
  }

  private List<InfrastructureDeploymentInfo> deleteInfrastructures(
      List<InfrastructureDeploymentInfo> toDelete) {
      List<InfrastructureDeploymentInfo> deleted = new ArrayList<>();
      for (InfrastructureDeploymentInfo infra : toDelete) {
          try {
              Deployer deployer = getDeployer(infra.getProvider());
              deployer.delete(infra);
              deleted.add(infra);
          } catch (ConfigurationException e) {
              logger.error("Error getting deployer for provider " + infra.getProvider().getApiType(), e);
          }
      }
      return deleted;
  }
}
