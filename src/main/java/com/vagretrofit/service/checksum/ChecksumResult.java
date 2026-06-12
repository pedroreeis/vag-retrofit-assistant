package com.vagretrofit.service.checksum;

import java.util.ArrayList;
import java.util.List;

/**
 * V2: Resultado de validação de checksum — suporta múltiplos checksums por dump.
 */
public class ChecksumResult {

    public enum Status {
        VALID,       // todos os checksums corretos
        INVALID,     // um ou mais checksums incorretos
        UNKNOWN,     // algoritmo desconhecido — não é possível validar
        REQUIRES_FLASH // módulo IMMO2: validação requer FLASH do MCU HC08
    }

    private final Status status;
    private final List<ChecksumEntry> entries;
    private final String message;

    public ChecksumResult(Status status, String message) {
        this.status  = status;
        this.message = message;
        this.entries = new ArrayList<>();
    }

    public void addEntry(int offset, int expected, int actual, boolean valid) {
        entries.add(new ChecksumEntry(offset, expected, actual, valid));
    }

    public Status getStatus()         { return status; }
    public String getMessage()        { return message; }
    public List<ChecksumEntry> getEntries() { return entries; }

    public boolean isValid()    { return status == Status.VALID; }
    public boolean isUnknown()  { return status == Status.UNKNOWN || status == Status.REQUIRES_FLASH; }

    /** Conta quantos checksums estão errados. */
    public long countInvalid() {
        return entries.stream().filter(e -> !e.isValid()).count();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d checksum(s), %d inválido(s))",
            status, message, entries.size(), countInvalid());
    }

    // ─── Inner record ────────────────────────────────────────────────────────

    public static class ChecksumEntry {
        private final int offset;
        private final int expected;
        private final int actual;
        private final boolean valid;

        public ChecksumEntry(int offset, int expected, int actual, boolean valid) {
            this.offset   = offset;
            this.expected = expected;
            this.actual   = actual;
            this.valid    = valid;
        }

        public int getOffset()   { return offset; }
        public int getExpected() { return expected; }
        public int getActual()   { return actual; }
        public boolean isValid() { return valid; }

        @Override
        public String toString() {
            return String.format("0x%04X: esperado=0x%02X, atual=0x%02X [%s]",
                offset, expected & 0xFF, actual & 0xFF, valid ? "OK" : "ERRO");
        }
    }
}
