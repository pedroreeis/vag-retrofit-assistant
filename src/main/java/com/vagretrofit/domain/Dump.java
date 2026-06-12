package com.vagretrofit.domain;

public class Dump {
    private byte[] data;
    private String filename;
    private String hashSha256;
    private RomVersion identifiedRomVersion;

    public Dump(byte[] data, String filename) {
        this.data = data;
        this.filename = filename;
    }

    public byte[] getData() { return data; }
    public String getFilename() { return filename; }
    
    public String getHashSha256() { return hashSha256; }
    public void setHashSha256(String hashSha256) { this.hashSha256 = hashSha256; }

    public RomVersion getIdentifiedRomVersion() { return identifiedRomVersion; }
    public void setIdentifiedRomVersion(RomVersion identifiedRomVersion) { this.identifiedRomVersion = identifiedRomVersion; }
}
