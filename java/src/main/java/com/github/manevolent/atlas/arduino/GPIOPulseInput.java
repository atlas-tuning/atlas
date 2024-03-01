package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;

public class GPIOPulseInput extends GPIOPin implements Input {
    private final GPIOEdgeType edgeType;
    private final int window;

    public GPIOPulseInput(String name, int pin, GPIOResistorMode resistorMode, GPIOEdgeType edgeType, int window) {
        super(name, pin, resistorMode, GPIOPinType.PWM);
        this.edgeType = edgeType;
        this.window = window;
    }

    public GPIOPulseInput(String name, int pin, GPIOResistorMode resistorMode) {
        this(name, pin, resistorMode, GPIOEdgeType.RISING, 64);
    }

    @Override
    public void write(Program program, BitWriter writer) throws IOException {
        super.write(program, writer);

        writer.write(edgeType.getFlag());
        writer.writeShort((short) (window & 0xFFFF));
    }

    @Override
    public float get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    public GPIOEdgeType getEdgeType() {
        return edgeType;
    }

    public int getWindow() {
        return window;
    }

    public enum SubValue implements com.github.manevolent.atlas.arduino.SubValue {
        NONE(0x0);

        private final int flag;

        SubValue(int flag) {
            this.flag = flag;
        }

        public int getCode() {
            return flag;
        }
    }
}
