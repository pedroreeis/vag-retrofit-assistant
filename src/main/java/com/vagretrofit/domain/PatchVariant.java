package com.vagretrofit.domain;

public class PatchVariant {
    private int id;
    private Patch patch;
    private RomVersion targetRomVersion;
    private String softwareVersion;
    private int addressStart;
    private int addressEnd;
    private byte[] patchData;
    private String clusterLevel;
    private String preconditions;
    private String safeguards;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Patch getPatch() { return patch; }
    public void setPatch(Patch patch) { this.patch = patch; }
    public RomVersion getTargetRomVersion() { return targetRomVersion; }
    public void setTargetRomVersion(RomVersion targetRomVersion) { this.targetRomVersion = targetRomVersion; }
    public String getSoftwareVersion() { return softwareVersion; }
    public void setSoftwareVersion(String softwareVersion) { this.softwareVersion = softwareVersion; }
    public int getAddressStart() { return addressStart; }
    public void setAddressStart(int addressStart) { this.addressStart = addressStart; }
    public int getAddressEnd() { return addressEnd; }
    public void setAddressEnd(int addressEnd) { this.addressEnd = addressEnd; }
    public byte[] getPatchData() { return patchData; }
    public void setPatchData(byte[] patchData) { this.patchData = patchData; }
    public String getClusterLevel() { return clusterLevel; }
    public void setClusterLevel(String clusterLevel) { this.clusterLevel = clusterLevel; }
    public String getPreconditions() { return preconditions; }
    public void setPreconditions(String preconditions) { this.preconditions = preconditions; }
    public String getSafeguards() { return safeguards; }
    public void setSafeguards(String safeguards) { this.safeguards = safeguards; }
}
