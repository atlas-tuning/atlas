package com.github.manevolent.atlas.connection;

public enum ConnectionMode {

    DISCONNECTED("Disconnected"),
    IDLE("Idle"),
    FLASH_ROM("Flash ROM"),
    DATALOG("Datalog"),
    READ_MEMORY("Read Memory"),
    WRITE_MEMORY("Write Memory");

    private final String name;
    ConnectionMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

}
