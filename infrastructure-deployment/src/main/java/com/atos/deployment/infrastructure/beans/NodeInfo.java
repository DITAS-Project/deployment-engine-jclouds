package com.atos.deployment.infrastructure.beans;

import java.util.List;

public class NodeInfo {

    private String name;
    private String role;
    private String ip;
    private String username;
    private String uuid;
    private List<String> driveUuids;
    private ServerStatusType status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<String> getDriveUuids() {
        return driveUuids;
    }

    public void setDriveUuids(List<String> driveUuids) {
        this.driveUuids = driveUuids;
    }

    public ServerStatusType getStatus() {
        return status;
    }

    public void setStatus(ServerStatusType status) {
        this.status = status;
    }
}
