package com.atos.deployment.frontend.rest.spark;

import com.atos.deployment.beans.ApiType;
import com.atos.deployment.beans.Deployment;
import com.atos.deployment.beans.DeploymentInfo;
import com.atos.deployment.beans.Infrastructure;
import com.atos.deployment.beans.InfrastructureDeploymentInfo;
import com.atos.deployment.infrastructure.deployers.CloudSigmaDeployer;
import com.atos.deployment.infrastructure.deployers.Deployer;
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

    path(
        "/v2",
        () -> {
          path(
              "/deployment",
              () -> {
                post(
                    "/",
                    (req, res) -> createDeployment(req, res, repository),
                    mapper::writeValueAsString);
                put(
                    "/:deploymentId/:infrastructureId",
                    (req, res) -> deployKubernetes(req, res, repository),
                    mapper::writeValueAsString);
                delete("/:deploymentId/:infrastructureId",
                        (req, res) -> deleteInfrastructure(req, res, repository),
                        mapper::writeValueAsString);
              });
        });
  }


  private static DeploymentInfo deleteInfrastructure(Request req, Response res, DeploymentRepository repository) {
    return null;
  }

  private static DeploymentInfo createDeployment(
      Request req, Response res, DeploymentRepository repository) {
    try {

      Deployment dep = getObjectMapper().readValue(req.body(), Deployment.class);

      DeploymentInfo result = new DeploymentInfo();
      result.setId(UUID.randomUUID().toString());
      result.setInfrastructures(new ArrayList<>());
      repository.save(result);

      for (Infrastructure infra : dep.getInfrastructure()) {
        InfrastructureDeploymentInfo infraResult = null;
        if (infra.getApiType() == ApiType.CLOUDSIGMA) {
          try {
            infraResult = new CloudSigmaDeployer().deploy(infra);
          } catch (ConfigurationException e) {
            logger.error("Error getting Cloudsigma configuration", e);
          }
        }

        if (infraResult != null) {
          infraResult.setInfraId(UUID.randomUUID().toString());
          result.getInfrastructures().add(infraResult);
          repository.update(result);
        }
      }

      res.type("application/json");
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
          Optional<InfrastructureDeploymentInfo> infrastructure =
              info.getInfrastructures()
                  .stream()
                  .filter(infra -> infraId.equals(infra.getInfraId()))
                  .findFirst();
          if (infrastructure.isPresent()) {
            InfrastructureDeploymentInfo update =
                getObjectMapper().readValue(req.body(), InfrastructureDeploymentInfo.class);

            if (update.isKubernetesDeployed() && !infrastructure.get().isKubernetesDeployed()) {
              KubernetesDeployer deployer =
                  new AnsibleKubernetesDeployer(
                      "/home/jose/Code/Ditas/deployment-engine-jclouds/kubernetes-deployment/src/main/resources/scripts/ansible",
                      "/home/jose");
              boolean success = deployer.deployKubernetesCluster(infrastructure.get());
              if (success) {
                infrastructure.get().setKubernetesDeployed(true);
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
