package com.vagretrofit.service;

import com.vagretrofit.domain.Dump;

import java.util.ArrayList;
import java.util.List;

public class DiffService {

    public static class DiffEntry {
        public final int address;
        public final byte originalByte;
        public final byte modifiedByte;

        public DiffEntry(int address, byte originalByte, byte modifiedByte) {
            this.address = address;
            this.originalByte = originalByte;
            this.modifiedByte = modifiedByte;
        }
        
        @Override
        public String toString() {
            return String.format("0x%04X : %02X -> %02X", address, originalByte, modifiedByte);
        }
    }

    public List<DiffEntry> computeDiff(Dump original, Dump modified) {
        List<DiffEntry> diffs = new ArrayList<>();
        byte[] origData = original.getData();
        byte[] modData = modified.getData();

        int length = Math.min(origData.length, modData.length);
        for (int i = 0; i < length; i++) {
            if (origData[i] != modData[i]) {
                diffs.add(new DiffEntry(i, origData[i], modData[i]));
            }
        }
        return diffs;
    }
}
