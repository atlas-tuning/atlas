package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;

public class GPIOOutput extends GPIOPin implements Output {
    private final Table source;
    private final Table holdTime;
    private final Value frequency;
    private final Value updateFrequency;

    public GPIOOutput(String name, int pin, GPIOResistorMode resistorMode, GPIOPinType type,
                      Table source, Table holdTime, Value frequency, Value updateFrequency) {
        super(name, pin, resistorMode, type);
        this.source = source;
        this.holdTime = holdTime;
        this.frequency = frequency;
        this.updateFrequency = updateFrequency;
    }

    public GPIOOutput(String name, int pin, GPIOResistorMode resistorMode, GPIOPinType type,
                      Table source) {
        this(name, pin, resistorMode, type, source, null, null, null);
    }

    @Override
    public void write(Program program, BitWriter writer) throws IOException {
        super.write(program, writer);

        if (program.getTables().contains(source)) {
            writer.writeShort((short) program.getTables().indexOf(source));
        } else {
            writer.writeShort((short) 0xFFFF);
        }

        if (program.getTables().contains(holdTime)) {
            writer.writeShort((short) program.getTables().indexOf(holdTime));
        } else {
            writer.writeShort((short) 0xFFFF);
        }

        if (updateFrequency instanceof Table && program.getTables().contains((Table) updateFrequency)) {
            writer.writeShort((short) program.getTables().indexOf(updateFrequency));
        } else {
            writer.writeShort((short) 0xFFFF);
            if (updateFrequency != null) {
                writer.writeFloatLE((int) updateFrequency.get());
            } else {
                writer.writeFloatLE(0.0f); // as fast as possible
            }
        }

        if (getType() == GPIOPinType.PWM) {
            if (frequency instanceof Table && program.getTables().contains((Table) frequency)) {
                writer.writeShort((short) program.getTables().indexOf(frequency));
            } else {
                writer.writeShort((short) 0xFFFF);
                if (frequency != null) {
                    writer.writeInt((int) frequency.get());
                } else {
                    throw new IllegalStateException("Frequency not supplied for PWM output");
                }
            }
        }
    }
}
