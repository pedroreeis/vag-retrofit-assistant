package com.vagretrofit.domain;

public class DumpMetadata {
    private int id;
    private String dumpHashSha256;
    private String dumpFilename;
    private String vin;
    private String pinSkc;
    private Integer keysAdapted;
    private Integer mileageKm;
    private String immoStatus;
    private String externalSoftware;
    private String externalSoftwareVersion;
    private String userNotes;
    private String createdAt;
    private String updatedAt;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDumpHashSha256() { return dumpHashSha256; }
    public void setDumpHashSha256(String dumpHashSha256) { this.dumpHashSha256 = dumpHashSha256; }
    public String getDumpFilename() { return dumpFilename; }
    public void setDumpFilename(String dumpFilename) { this.dumpFilename = dumpFilename; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getPinSkc() { return pinSkc; }
    public void setPinSkc(String pinSkc) { this.pinSkc = pinSkc; }
    public Integer getKeysAdapted() { return keysAdapted; }
    public void setKeysAdapted(Integer keysAdapted) { this.keysAdapted = keysAdapted; }
    public Integer getMileageKm() { return mileageKm; }
    public void setMileageKm(Integer mileageKm) { this.mileageKm = mileageKm; }
    public String getImmoStatus() { return immoStatus; }
    public void setImmoStatus(String immoStatus) { this.immoStatus = immoStatus; }
    public String getExternalSoftware() { return externalSoftware; }
    public void setExternalSoftware(String externalSoftware) { this.externalSoftware = externalSoftware; }
    public String getExternalSoftwareVersion() { return externalSoftwareVersion; }
    public void setExternalSoftwareVersion(String externalSoftwareVersion) { this.externalSoftwareVersion = externalSoftwareVersion; }
    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
