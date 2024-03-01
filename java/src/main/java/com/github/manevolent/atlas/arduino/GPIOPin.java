package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;

public abstract class GPIOPin implements Writable {
    private final String name;
    private final int pin;
    private final GPIOResistorMode resistorMode;
    private final GPIOPinType type;

    protected GPIOPin(String name, int pin, GPIOResistorMode resistorMode, GPIOPinType type) {
        this.name = name;
        this.pin = pin;
        this.resistorMode = resistorMode;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getPin() {
        return pin;
    }

    public GPIOResistorMode getResistorMode() {
        return resistorMode;
    }

    public GPIOPinType getType() {
        return type;
    }

    @Override
    public void write(Program program, BitWriter writer) throws IOException {
        writeAscii(writer, name);
        writer.write(pin);
        writer.write(getResistorMode().getFlag());
        writer.write(getType().getFlag());
    }
}
