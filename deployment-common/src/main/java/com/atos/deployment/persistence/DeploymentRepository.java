package com.atos.deployment.persistence;

import com.atos.deployment.beans.DeploymentInfo;

import java.util.List;

public interface DeploymentRepository {

    DeploymentInfo save(DeploymentInfo deployment);
    DeploymentInfo get(String deploymentId);
    List<DeploymentInfo> list();
    DeploymentInfo update(DeploymentInfo deployment);
    DeploymentInfo delete(String deploymentId);

}
