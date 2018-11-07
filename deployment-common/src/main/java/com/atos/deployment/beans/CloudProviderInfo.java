package com.atos.deployment.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CloudProviderInfo {
    @JsonProperty("api_endpoint")
    private String apiEndpoint;

    @JsonProperty("api_type")
    private ApiType apiType;

    @JsonProperty("keypair_id")
    private String keyparId;

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
}
