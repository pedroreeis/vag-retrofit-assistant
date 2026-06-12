package com.vagretrofit.domain;

/**
 * Patch adicionado manualmente pelo operador (não faz parte da KB oficial).
 * V2: distinguido da KB por badge visual na UI.
 */
public class OperatorPatchEntry {
    private int id;
    private String moduleAddress;   // endereço VAG (17, 46, etc.)
    private String targetRomId;     // ROM ID alvo
    private String patchName;
    private String patchDescription;
    private int addressStart;
    private int addressEnd;
    private byte[] patchData;
    private String patchDataHex;
    private String notes;
    private boolean verified;       // true = operador confirmou que testou
    private String createdAt;
    private String updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getModuleAddress() { return moduleAddress; }
    public void setModuleAddress(String moduleAddress) { this.moduleAddress = moduleAddress; }

    public String getTargetRomId() { return targetRomId; }
    public void setTargetRomId(String targetRomId) { this.targetRomId = targetRomId; }

    public String getPatchName() { return patchName; }
    public void setPatchName(String patchName) { this.patchName = patchName; }

    public String getPatchDescription() { return patchDescription; }
    public void setPatchDescription(String patchDescription) { this.patchDescription = patchDescription; }

    public int getAddressStart() { return addressStart; }
    public void setAddressStart(int addressStart) { this.addressStart = addressStart; }

    public int getAddressEnd() { return addressEnd; }
    public void setAddressEnd(int addressEnd) { this.addressEnd = addressEnd; }

    public byte[] getPatchData() { return patchData; }
    public void setPatchData(byte[] patchData) { this.patchData = patchData; }

    public String getPatchDataHex() { return patchDataHex; }
    public void setPatchDataHex(String patchDataHex) { this.patchDataHex = patchDataHex; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("[OPERADOR] %s → ROM %s @ 0x%04X (verified=%s)",
            patchName, targetRomId, addressStart, verified);
    }
}
