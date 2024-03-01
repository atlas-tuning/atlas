package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;

public class GPIOInput extends GPIOPin implements Input {
    private final Input v_gnd, v_ref;

    public GPIOInput(String name, int pin, GPIOResistorMode resistorMode, GPIOPinType type,
                     Input vGnd, Input vRef) {
        super(name, pin, resistorMode, type);

        if (type == GPIOPinType.PWM) {
            throw new UnsupportedOperationException("See " + GPIOPulseInput.class);
        }

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

    public enum SubValue implements com.github.manevolent.atlas.arduino.SubValue {
        NONE(0x0),
        SECONDS_SINCE_LAST_CHANGE(0x1),
        DELTA(0x2);

        private final int flag;

        SubValue(int flag) {
            this.flag = flag;
        }

        public int getCode() {
            return flag;
        }
    }
}
