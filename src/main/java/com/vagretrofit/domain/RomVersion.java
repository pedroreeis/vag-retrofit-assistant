package com.vagretrofit.domain;

public class RomVersion {
    private int id;
    private Module module;
    private String romId;
    private String stickerCode;
    private String romIdOffset;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }
    public String getRomId() { return romId; }
    public void setRomId(String romId) { this.romId = romId; }
    public String getStickerCode() { return stickerCode; }
    public void setStickerCode(String stickerCode) { this.stickerCode = stickerCode; }
    public String getRomIdOffset() { return romIdOffset; }
    public void setRomIdOffset(String romIdOffset) { this.romIdOffset = romIdOffset; }

    @Override
    public String toString() {
        return String.format("ROM %s (%s)", romId, stickerCode != null ? stickerCode : "N/A");
    }
}
