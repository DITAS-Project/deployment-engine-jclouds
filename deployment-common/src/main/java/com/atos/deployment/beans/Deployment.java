package com.atos.deployment.beans;

import java.util.List;

public class Deployment {

    private String name;
    private String description;
    private List<Infrastructure> infrastructure;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Infrastructure> getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(List<Infrastructure> infrastructure) {
        this.infrastructure = infrastructure;
    }
}
