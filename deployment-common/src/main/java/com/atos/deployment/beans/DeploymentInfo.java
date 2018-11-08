package com.atos.deployment.beans;

import java.util.List;

public class DeploymentInfo {
    private String id;
    private List<InfrastructureDeploymentInfo> infrastructures;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<InfrastructureDeploymentInfo> getInfrastructures() {
        return infrastructures;
    }

    public void setInfrastructures(List<InfrastructureDeploymentInfo> infrastructures) {
        this.infrastructures = infrastructures;
    }

    public InfrastructureDeploymentInfo getInfrastructure(String infraId) {
        return this
                .getInfrastructures()
                .stream()
                .filter(infra -> infra.getInfraId().equals(infraId))
                .findFirst()
                .get();
    }
}
