package com.vagretrofit.service;

import com.vagretrofit.domain.AuditEntry;
import com.vagretrofit.repository.AuditRepository;
import java.time.Instant;

public class AuditService {

    private final AuditRepository repository;

    public AuditService() {
        this.repository = new AuditRepository();
    }

    public void logOperation(String operationType, String filename, String hashBefore, String hashAfter, 
                             String moduleIdentified, String patchApplied, String result, String diff) {
        AuditEntry entry = new AuditEntry();
        entry.setTimestamp(Instant.now().toString());
        entry.setOperationType(operationType);
        entry.setDumpFilename(filename);
        entry.setDumpHashBefore(hashBefore);
        entry.setDumpHashAfter(hashAfter);
        entry.setModuleIdentified(moduleIdentified);
        entry.setPatchApplied(patchApplied);
        entry.setResult(result);
        entry.setDiffHex(diff);
        
        repository.save(entry);
    }
}
