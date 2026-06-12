package com.vagretrofit.domain;

/**
 * Endereços VAG conhecidos para módulos no barramento K-Line / CAN.
 * V2: suporte multi-módulo além do painel de instrumentos.
 */
public enum ModuleAddress {
    ENGINE         ("01", "Motor (ECU)"),
    AUTO_GEARBOX   ("02", "Câmbio Automático"),
    ABS            ("03", "ABS"),
    AIR_CONDITIONING("08", "Ar Condicionado"),
    CENTRAL_ELECTRIC("09", "Elétrica Central / BCM"),
    INSTRUMENTS    ("17", "Painel de Instrumentos"),
    AUXILIARY_HEAT ("04", "Aquecimento Auxiliar"),
    AIRBAG         ("15", "Airbag"),
    COMFORT        ("46", "Módulo de Conforto (CCM)"),
    RADIO          ("56", "Rádio"),
    GATEWAY        ("19", "Gateway"),
    IMMOBILIZER    ("25", "Imobilizador"),
    STEERING       ("44", "Direção Elétrica"),
    DISTANCE_REGULATION("76", "Regulador de Distância");

    private final String code;
    private final String description;

    ModuleAddress(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

    public static ModuleAddress fromCode(String code) {
        for (ModuleAddress addr : values()) {
            if (addr.code.equalsIgnoreCase(code)) return addr;
        }
        return null;
    }

    @Override
    public String toString() {
        return code + " — " + description;
    }
}
