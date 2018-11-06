package com.atos.deployment.infrastructure.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Resource {

    private String name;
    @JsonProperty("instance_type")
    private String instanceType;
    private Integer cpu;
    private Integer cores;
    private Long ram;
    private Long disk;
    @JsonProperty("generate_ssh_keys")
    private Boolean generateSshKeys;
    @JsonProperty("ssh_keys_id")
    private String sshKeysId;
    private RoleType role;
    @JsonProperty("image_id")
    private String imageId;
    private String ip;
    private List<Drive> drives;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Long getRam() {
        return ram;
    }

    public void setRam(Long ram) {
        this.ram = ram;
    }

    public Long getDisk() {
        return disk;
    }

    public void setDisk(Long disk) {
        this.disk = disk;
    }

    public Boolean getGenerateSshKeys() {
        return generateSshKeys;
    }

    public void setGenerateSshKeys(Boolean generateSshKeys) {
        this.generateSshKeys = generateSshKeys;
    }

    public String getSshKeysId() {
        return sshKeysId;
    }

    public void setSshKeysId(String sshKeysId) {
        this.sshKeysId = sshKeysId;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getCores() {
        return cores;
    }

    public void setCores(Integer cores) {
        this.cores = cores;
    }

    public List<Drive> getDrives() {
        return drives;
    }

    public void setDrives(List<Drive> drives) {
        this.drives = drives;
    }
}
