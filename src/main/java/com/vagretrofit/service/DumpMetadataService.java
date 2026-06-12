package com.vagretrofit.service;

import com.vagretrofit.domain.DumpMetadata;
import com.vagretrofit.repository.DumpMetadataRepository;

public class DumpMetadataService {

    private final DumpMetadataRepository repository;

    public DumpMetadataService() {
        this.repository = new DumpMetadataRepository();
    }

    public void registerExternalMetadata(String hashSha256, String filename, String vin, String pinSkc, 
                                         Integer keys, Integer mileage, String immoStatus, String externalSoftware) {
        
        if (externalSoftware == null || externalSoftware.trim().isEmpty()) {
            throw new IllegalArgumentException("Software Externo Utilizado é obrigatório para rastreabilidade.");
        }

        DumpMetadata metadata = new DumpMetadata();
        metadata.setDumpHashSha256(hashSha256);
        metadata.setDumpFilename(filename);
        metadata.setVin(vin);
        metadata.setPinSkc(pinSkc);
        metadata.setKeysAdapted(keys);
        metadata.setMileageKm(mileage);
        metadata.setImmoStatus(immoStatus);
        metadata.setExternalSoftware(externalSoftware);
        
        repository.save(metadata);
        System.out.println("Metadados registrados com sucesso vinculados ao hash: " + hashSha256);
    }
}
