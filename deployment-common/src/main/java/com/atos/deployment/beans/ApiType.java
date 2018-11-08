package com.atos.deployment.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ApiType {
    @JsonProperty("cloudsigma")
    CLOUDSIGMA,
    GCP,
    AWS,
    AZURE,
    OPENSTACK,
    EDGE
}
