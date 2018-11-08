package com.atos.deployment.frontend.rest.spark;

import com.atos.deployment.beans.CloudProviderInfo;
import com.atos.deployment.beans.Deployment;
import com.atos.deployment.beans.DeploymentInfo;
import com.atos.deployment.beans.Infrastructure;
import com.atos.deployment.beans.InfrastructureDeploymentInfo;
import com.atos.deployment.infrastructure.deployers.CloudSigmaDeployer;
import com.atos.deployment.infrastructure.deployers.Deployer;
import com.atos.deployment.infrastructure.deployers.MultiCloudDeployer;
import com.atos.deployment.kubernetes.KubernetesDeployer;
import com.atos.deployment.kubernetes.ansible.AnsibleKubernetesDeployer;
import com.atos.deployment.persistence.DeploymentRepository;
import com.atos.deployment.persistence.mongo.MongoDeploymentRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static spark.Spark.delete;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static ObjectMapper getObjectMapper() {
    return new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  public static void main(String[] args) {

    ObjectMapper mapper = getObjectMapper();

    DeploymentRepository repository = new MongoDeploymentRepository();

    MultiCloudDeployer deployer = new MultiCloudDeployer();

    path(
        "/v2",
        () -> {
          path(
              "/deployment",
              () -> {
                post(
                    "",
                    (req, res) -> createDeployment(req, res, repository, deployer),
                    mapper::writeValueAsString);
                put(
                    "/:deploymentId/:infrastructureId",
                    (req, res) -> deployKubernetes(req, res, repository),
                    mapper::writeValueAsString);
                delete(
                    "/:deploymentId/:infrastructureId",
                    (req, res) -> deleteInfrastructure(req, res, repository, deployer),
                    mapper::writeValueAsString);
              });
        });
  }

  private static DeploymentInfo deleteInfrastructure(
      Request req, Response res, DeploymentRepository repository, MultiCloudDeployer deployer) {

    String deploymentId = req.params("deploymentId");
    String infraId = req.params("infrastructureId");
    if (deploymentId != null) {
      DeploymentInfo deployment = repository.get(deploymentId);
      if (deployment != null) {
        deployment = deployer.deleteInfrastructure(deployment, infraId);
        if (deployment.getInfrastructures().isEmpty()) {
          repository.delete(deployment.getId());
        } else {
          repository.update(deployment);
        }
        return deployment;
      } else {
        logger.error("Can't find deployment with id " + deploymentId);
        res.status(404);
      }
    } else {
      logger.error("Received call without deployment id");
      res.status(400);
    }

    return null;
  }

  private static DeploymentInfo createDeployment(
      Request req, Response res, DeploymentRepository repository, MultiCloudDeployer deployer) {
    try {

      Deployment dep = getObjectMapper().readValue(req.body(), Deployment.class);

      DeploymentInfo result = deployer.createDeployment(dep);
      repository.save(result);

      return result;

    } catch (IOException e) {
      logger.error("Error deserializing deployment object", e);
      res.status(400);
    }
    return null;
  }

  private static DeploymentInfo deployKubernetes(
      Request req, Response res, DeploymentRepository repository) {
    String deploymentId = req.params("deploymentId");
    String infraId = req.params("infrastructureId");

    if (deploymentId != null && infraId != null) {
      try {
        DeploymentInfo info = repository.get(deploymentId);
        if (info != null) {
          InfrastructureDeploymentInfo infrastructure = info.getInfrastructure(infraId);

          if (infrastructure != null) {
            InfrastructureDeploymentInfo update =
                getObjectMapper().readValue(req.body(), InfrastructureDeploymentInfo.class);

            if (update.isKubernetesDeployed() && !infrastructure.isKubernetesDeployed()) {
              KubernetesDeployer deployer =
                  new AnsibleKubernetesDeployer(
                      "/home/jose/Code/Ditas/deployment-engine-jclouds/kubernetes-deployment/src/main/resources/scripts/ansible",
                      "/home/jose");
              boolean success = deployer.deployKubernetesCluster(infrastructure);
              if (success) {
                infrastructure.setKubernetesDeployed(true);
                repository.update(info);
                return info;
              }
            }
          }
        }

      } catch (IOException e) {
        logger.error("Error deserializing update object", e);
        res.status(400);
      }
    }

    return null;
  }
}
