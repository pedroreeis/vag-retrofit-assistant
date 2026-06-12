package com.vagretrofit.service;

import com.vagretrofit.domain.Dump;
import com.vagretrofit.repository.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentificationServiceTest {

    @BeforeAll
    static void setup() {
        DatabaseManager.initialize();
    }

    @Test
    void testIdentify_Success_00A4() {
        IdentificationService service = new IdentificationService();
        byte[] dumpData = new byte[2048];
        
        // Mock ROM ID 00A4 at offset 0x4F8 (VWK501MH)
        dumpData[0x4F8] = 0x00;
        dumpData[0x4F9] = (byte) 0xA4;

        Dump dump = new Dump(dumpData, "test_dump.bin");
        service.identify(dump);

        assertNotNull(dump.getIdentifiedRomVersion());
        assertEquals("00A4", dump.getIdentifiedRomVersion().getRomId());
        assertEquals("VWK501MH", dump.getIdentifiedRomVersion().getModule().getSoftwareVersion());
        assertEquals("IMMO3", dump.getIdentifiedRomVersion().getModule().getImmo());
    }

    @Test
    void testIdentify_Success_00AE() {
        IdentificationService service = new IdentificationService();
        byte[] dumpData = new byte[2048];
        
        // Mock ROM ID 00AE at offset 0x4FA (VWK503MH)
        dumpData[0x4FA] = 0x00;
        dumpData[0x4FB] = (byte) 0xAE;

        Dump dump = new Dump(dumpData, "test_dump_503.bin");
        service.identify(dump);

        assertNotNull(dump.getIdentifiedRomVersion());
        assertEquals("00AE", dump.getIdentifiedRomVersion().getRomId());
        assertEquals("VWK503MH", dump.getIdentifiedRomVersion().getModule().getSoftwareVersion());
    }

    @Test
    void testIdentify_Fail_WrongSize() {
        IdentificationService service = new IdentificationService();
        byte[] dumpData = new byte[1024]; // Invalid size
        
        Dump dump = new Dump(dumpData, "test_dump.bin");
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            service.identify(dump);
        });
        
        assertTrue(exception.getMessage().contains("Tamanho de dump inválido"));
    }

    @Test
    void testIdentify_Fail_UnknownRomId() {
        IdentificationService service = new IdentificationService();
        byte[] dumpData = new byte[2048];
        
        // Mock Unknown ROM ID FF FF
        dumpData[0x4F8] = (byte) 0xFF;
        dumpData[0x4F9] = (byte) 0xFF;

        Dump dump = new Dump(dumpData, "test_dump.bin");
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            service.identify(dump);
        });
        
        assertTrue(exception.getMessage().contains("DESCONHECIDO"));
    }
}
