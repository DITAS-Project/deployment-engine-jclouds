package com.atos.deployment.infrastructure.deployers;

import com.atos.deployment.infrastructure.beans.DeploymentInfo;
import com.atos.deployment.infrastructure.beans.Infrastructure;
import com.atos.deployment.infrastructure.beans.InfrastructureDeploymentInfo;

public interface Deployer {

    InfrastructureDeploymentInfo deploy(Infrastructure infrastructure);

}
