package com.atos.deployment.infrastructure.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Infrastructure {

    private String name;
    private String description;
    private String type;

    @JsonProperty("on-line")
    private Boolean online;

    @JsonProperty("api_endpoint")
    private String apiEndpoint;

    @JsonProperty("api_type")
    private ApiType apiType;

    @JsonProperty("keypair_id")
    private String keyparId;

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

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public ApiType getApiType() {
        return apiType;
    }

    public void setApiType(ApiType apiType) {
        this.apiType = apiType;
    }

    public String getKeyparId() {
        return keyparId;
    }

    public void setKeyparId(String keyparId) {
        this.keyparId = keyparId;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
