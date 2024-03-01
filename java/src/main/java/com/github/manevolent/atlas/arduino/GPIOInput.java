package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;

public class GPIOInput extends GPIOPin implements Input {
    private final Input v_gnd, v_ref;

    public GPIOInput(String name, int pin, GPIOResistorMode resistorMode, GPIOPinType type,
                     Input vGnd, Input vRef) {
        super(name, pin, resistorMode, type);
        v_gnd = vGnd;
        v_ref = vRef;
    }


    public GPIOInput(String name, int pin, GPIOResistorMode resistorMode, GPIOPinType type) {
        this(name, pin, resistorMode, type, null, null);
    }

    public Input getVGnd() {
        return v_gnd;
    }

    public Input getVRef() {
        return v_ref;
    }

    @Override
    public void write(Program program, BitWriter writer) throws IOException {
        super.write(program, writer);

        if (v_gnd != null) {
            writer.write(program.getInputs().indexOf(v_gnd));
        } else {
            writer.write(0xFF);
        }

        if (v_ref != null) {
            writer.write(program.getInputs().indexOf(v_ref));
        } else {
            writer.write(0xFF);
        }
    }

    @Override
    public float get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
