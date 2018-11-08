package com.atos.deployment.infrastructure.deployers;

import com.atos.deployment.beans.Infrastructure;
import com.atos.deployment.beans.InfrastructureDeploymentInfo;

public interface Deployer {

    InfrastructureDeploymentInfo deploy(Infrastructure infrastructure);
    InfrastructureDeploymentInfo delete(InfrastructureDeploymentInfo infrastructure);

}
