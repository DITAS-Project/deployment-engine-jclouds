package com.atos.deployment.kubernetes.ansible;

import com.atos.deployment.infrastructure.beans.DeploymentInfo;
import com.atos.deployment.kubernetes.KubernetesDeployer;

public class AnsibleKubernetesDeployer implements KubernetesDeployer {

    @Override
    public boolean deployKubernetes(DeploymentInfo deployment) {
        return false;
    }

}
