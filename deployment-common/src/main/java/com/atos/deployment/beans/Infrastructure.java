package com.atos.deployment.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Infrastructure {

    private String name;
    private String description;
    private String type;

    @JsonProperty("on-line")
    private Boolean online;

    private CloudProviderInfo provider;

    private List<Resource> resources;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public CloudProviderInfo getProvider() {
        return provider;
    }

    public void setProvider(CloudProviderInfo provider) {
        this.provider = provider;
    }
}
