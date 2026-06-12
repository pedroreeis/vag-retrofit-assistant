package com.vagretrofit.service;

import com.vagretrofit.domain.Dump;
import com.vagretrofit.domain.Module;
import com.vagretrofit.domain.Patch;
import com.vagretrofit.domain.PatchVariant;
import com.vagretrofit.domain.RomVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatchServiceTest {

    private PatchService patchService;

    @BeforeEach
    void setup() {
        ValidationService validationService = new ValidationService();
        patchService = new PatchService(validationService);
    }

    @Test
    void testApplyPatch_Success() {
        byte[] dumpData = new byte[2048];
        dumpData[0x4F8] = 0x00;
        dumpData[0x4F9] = (byte) 0xA4;

        Module module = new Module();
        module.setPartNumber("1J0920826J"); // V2: part number válido
        module.setSoftwareVersion("VWK501MH");
        module.setModuleAddress("17");
        module.setImmo("IMMO3");
        module.setChecksumAlgorithm("VDO_PQ34_IMMO3");
        module.setEepromSizeBytes(2048);

        RomVersion romVersion = new RomVersion();
        romVersion.setId(1);
        romVersion.setModule(module);

        Dump dump = new Dump(dumpData, "test.bin");
        dump.setIdentifiedRomVersion(romVersion);

        Patch patch = new Patch();
        patch.setName("Needle Sweep");
        patch.setAuthor("Graeme's Webspace");

        PatchVariant variant = new PatchVariant();
        variant.setPatch(patch);
        variant.setTargetRomVersion(romVersion);
        variant.setSoftwareVersion("VWK501MH");
        variant.setAddressStart(0x4F4);
        variant.setAddressEnd(0x4F6);
        variant.setPatchData(new byte[]{(byte)0xF0, 0x1A, 0x00}); // Graeme 00A4 known pattern

        Dump modifiedDump = patchService.applyPatch(dump, variant);

        assertNotNull(modifiedDump);
        assertEquals((byte)0xF0, modifiedDump.getData()[0x4F4]);
        assertEquals((byte)0x1A, modifiedDump.getData()[0x4F5]);
        assertEquals(0x00, modifiedDump.getData()[0x4F6]);

        // Ensure ROM ID area wasn't changed (VWK501MH: 0x4F8–0x4F9)
        assertEquals(0x00, modifiedDump.getData()[0x4F8]);
        assertEquals((byte) 0xA4, modifiedDump.getData()[0x4F9]);
    }

    @Test
    void testApplyPatch_Fail_RomMismatch() {
        byte[] dumpData = new byte[2048];

        Module module = new Module();
        module.setPartNumber("1J0920826J"); // V2: part number válido
        module.setSoftwareVersion("VWK501MH");
        module.setModuleAddress("17");
        module.setImmo("IMMO3");
        module.setChecksumAlgorithm("VDO_PQ34_IMMO3");
        module.setEepromSizeBytes(2048);

        RomVersion romVersion = new RomVersion();
        romVersion.setId(1); // Dump has ID 1
        romVersion.setModule(module);

        Dump dump = new Dump(dumpData, "test.bin");
        dump.setIdentifiedRomVersion(romVersion);

        RomVersion wrongRomVersion = new RomVersion();
        wrongRomVersion.setId(2); // Patch targets ID 2

        PatchVariant variant = new PatchVariant();
        variant.setTargetRomVersion(wrongRomVersion);
        variant.setSoftwareVersion("VWK501MH");

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            patchService.applyPatch(dump, variant);
        });

        assertTrue(exception.getMessage().contains("Patch incompatível"));
    }
}
