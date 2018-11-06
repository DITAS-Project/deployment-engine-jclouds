package com.atos.deployment.infrastructure.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RoleType {
    @JsonProperty("master")
    MASTER,
    @JsonProperty("slave")
    SLAVE
}
