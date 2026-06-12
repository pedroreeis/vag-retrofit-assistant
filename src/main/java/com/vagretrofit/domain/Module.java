package com.vagretrofit.domain;

public class Module {
    private int id;
    private String platformCode;
    private String manufacturer;
    private String partNumber;
    private String softwareVersion;
    private String eepromType;
    private int eepromSizeBytes;
    private String immo;
    // V2: endereço do módulo no barramento VAG (17=Instruments, 46=Comfort, etc.)
    private String moduleAddress;
    // V2: informações de checksum
    private String checksumAlgorithm;
    private int checksumCount;
    private boolean checksumRequiresFlash;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getSoftwareVersion() { return softwareVersion; }
    public void setSoftwareVersion(String softwareVersion) { this.softwareVersion = softwareVersion; }
    public String getEepromType() { return eepromType; }
    public void setEepromType(String eepromType) { this.eepromType = eepromType; }
    public int getEepromSizeBytes() { return eepromSizeBytes; }
    public void setEepromSizeBytes(int eepromSizeBytes) { this.eepromSizeBytes = eepromSizeBytes; }
    public String getImmo() { return immo; }
    public void setImmo(String immo) { this.immo = immo; }
    public String getModuleAddress() { return moduleAddress; }
    public void setModuleAddress(String moduleAddress) { this.moduleAddress = moduleAddress; }
    public String getChecksumAlgorithm() { return checksumAlgorithm; }
    public void setChecksumAlgorithm(String checksumAlgorithm) { this.checksumAlgorithm = checksumAlgorithm; }
    public int getChecksumCount() { return checksumCount; }
    public void setChecksumCount(int checksumCount) { this.checksumCount = checksumCount; }
    public boolean isChecksumRequiresFlash() { return checksumRequiresFlash; }
    public void setChecksumRequiresFlash(boolean checksumRequiresFlash) { this.checksumRequiresFlash = checksumRequiresFlash; }

    /**
     * Retorna true se o algoritmo de checksum é conhecido e pode ser calculado.
     */
    public boolean hasKnownChecksum() {
        return checksumAlgorithm != null
            && !checksumAlgorithm.startsWith("UNKNOWN")
            && !checksumAlgorithm.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %s, %s, addr=%s)", partNumber, softwareVersion, manufacturer, immo, moduleAddress);
    }
}
