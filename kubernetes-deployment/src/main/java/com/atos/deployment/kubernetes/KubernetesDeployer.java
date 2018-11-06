package com.atos.deployment.kubernetes;

import com.atos.deployment.infrastructure.beans.Deployment;
import com.atos.deployment.infrastructure.beans.DeploymentInfo;

public interface KubernetesDeployer {

    boolean deployKubernetes(DeploymentInfo deployment);

}
