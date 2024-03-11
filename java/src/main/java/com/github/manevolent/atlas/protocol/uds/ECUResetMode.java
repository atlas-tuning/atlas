package com.github.manevolent.atlas.protocol.uds;

public enum ECUResetMode {
    HARD_RESET("Hard Reset", 0x01),
    SOFT_RESET("Soft Reset", 0x03),
    KEY_CYCLE("Key Cycle", 0x02),
    ENABLE_RAPID_POWER_SHUTDOWN("Enable Rapid Power Shutdown", 0x04),
    DISABLE_RAPID_POWER_SHUTDOWN("Disable Rapid Power Shutdown", 0x05);

    private final String name;
    private final int code;
    ECUResetMode(String name, int code) {
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getCode() {
        return code;
    }


}