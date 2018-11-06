package com.atos.deployment.infrastructure.beans;

import java.util.List;

public class InfrastructureDeploymentInfo {
    private String id;
    private String type;
    private NodeInfo master;
    private List<NodeInfo> slaves;
    private boolean kubernetesDeployed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NodeInfo getMaster() {
        return master;
    }

    public void setMaster(NodeInfo master) {
        this.master = master;
    }

    public List<NodeInfo> getSlaves() {
        return slaves;
    }

    public void setSlaves(List<NodeInfo> slaves) {
        this.slaves = slaves;
    }

    public boolean isKubernetesDeployed() {
        return kubernetesDeployed;
    }

    public void setKubernetesDeployed(boolean kubernetesDeployed) {
        this.kubernetesDeployed = kubernetesDeployed;
    }
}
