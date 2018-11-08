package com.atos.deployment.persistence.mongo;

import com.atos.deployment.beans.DeploymentInfo;
import com.atos.deployment.persistence.DeploymentRepository;
import com.mongodb.client.MongoCollection;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoDeploymentRepository extends MongoRepository implements DeploymentRepository {

  MongoCollection<DeploymentInfo> collection;

  public MongoDeploymentRepository() {
    collection =
        client.getDatabase("deployment-engine").getCollection("deployments", DeploymentInfo.class);
  }

  @Override
  public DeploymentInfo save(DeploymentInfo deployment) {
    collection.insertOne(deployment);
    return deployment;
  }

  @Override
  public DeploymentInfo get(String deploymentId) {
    return collection.find(eq("_id", deploymentId)).first();
  }

  @Override
  public List<DeploymentInfo> list() {
    return collection.find().into(new ArrayList<>());
  }

  @Override
  public DeploymentInfo update(DeploymentInfo deployment) {
    collection.replaceOne(eq("_id", deployment.getId()), deployment);
    return deployment;
  }

  @Override
  public DeploymentInfo delete(String deploymentId) {
    return collection.findOneAndDelete(eq("_id", deploymentId));
  }
}
