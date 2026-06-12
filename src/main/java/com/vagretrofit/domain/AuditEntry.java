package com.vagretrofit.domain;

public class AuditEntry {
    private int id;
    private String timestamp;
    private String operationType;
    private String dumpFilename;
    private String dumpHashBefore;
    private String dumpHashAfter;
    private String moduleIdentified;
    private String patchApplied;
    private Integer patchVariantId;
    private String result;
    private String blockReason;
    private String diffHex;
    private String userNotes;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getDumpFilename() { return dumpFilename; }
    public void setDumpFilename(String dumpFilename) { this.dumpFilename = dumpFilename; }
    public String getDumpHashBefore() { return dumpHashBefore; }
    public void setDumpHashBefore(String dumpHashBefore) { this.dumpHashBefore = dumpHashBefore; }
    public String getDumpHashAfter() { return dumpHashAfter; }
    public void setDumpHashAfter(String dumpHashAfter) { this.dumpHashAfter = dumpHashAfter; }
    public String getModuleIdentified() { return moduleIdentified; }
    public void setModuleIdentified(String moduleIdentified) { this.moduleIdentified = moduleIdentified; }
    public String getPatchApplied() { return patchApplied; }
    public void setPatchApplied(String patchApplied) { this.patchApplied = patchApplied; }
    public Integer getPatchVariantId() { return patchVariantId; }
    public void setPatchVariantId(Integer patchVariantId) { this.patchVariantId = patchVariantId; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
    public String getDiffHex() { return diffHex; }
    public void setDiffHex(String diffHex) { this.diffHex = diffHex; }
    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
}
