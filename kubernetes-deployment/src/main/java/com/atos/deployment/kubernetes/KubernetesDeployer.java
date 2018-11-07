package com.atos.deployment.kubernetes;

import com.atos.deployment.beans.InfrastructureDeploymentInfo;

public interface KubernetesDeployer {

    boolean deployKubernetesCluster(InfrastructureDeploymentInfo deployment);

}
