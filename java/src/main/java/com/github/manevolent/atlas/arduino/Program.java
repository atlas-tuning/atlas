package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Program {
    private static final short VERSION = 2;

    private final List<Input> inputs;
    private final List<Table> tables;
    private final List<Output> outputs;

    public Program(List<Input> inputs, List<Table> tables, List<Output> outputs) {
        this.inputs = inputs;
        this.tables = tables;
        this.outputs = outputs;
    }

    public Program() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public Input fromInput(int index) {
        return inputs.get(index);
    }

    public Input fromInput(String name) {
        return inputs.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown input " + name));
    }

    public Table fromTable(int index) {
        return tables.get(index);
    }

    public Table fromTable(String name) {
        return tables.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown table " + name));
    }

    public List<Table> getTables() {
        return tables;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public void write(BitWriter writer) throws IOException {
        writer.writeShort(VERSION);

        writer.write(getInputs().size() & 0xFF);
        for (Input i : getInputs()) {
            i.write(this, writer);
        }

        writer.writeShort((short)(getTables().size() & 0xFFFF));
        for (Table t : getTables()) {
            t.write(this, writer);
        }

        writer.write(getOutputs().size() & 0xFF);
        for (Output o : getOutputs()) {
            o.write(this, writer);
        }

        // Busses not implemented
        writer.write(0x00);
    }

    public void read(BitReader reader) throws IOException {
        int version = reader.readShort();
        if (version != VERSION) {
            throw new IllegalArgumentException("Unexpected version " + version);
        }


    }

    public static void main(String[] args) throws IOException {
        Program program = new Program();

        program.getInputs().add(new GPIOInput(
                "v_gnd",
                35,
                GPIOResistorMode.NONE,
                GPIOPinType.ANALOG
        ));

        program.getInputs().add(new GPIOInput(
                "v_ref",
                32,
                GPIOResistorMode.NONE,
                GPIOPinType.ANALOG
        ));

        program.getInputs().add(new GPIOInput(
                "pin_34",
                34,
                GPIOResistorMode.NONE,
                GPIOPinType.ANALOG,
                program.fromInput(0),
                program.fromInput(1)
        ));

        program.getTables().add(new Table(
                "invert",
                Collections.singletonList(new Dimension(
                        program.fromInput("pin_34"),
                        Integration.LINEAR,
                        new float[] { 0f, 5f }
                )),
                new float[] { 5f, 0f }
        ));

        program.getOutputs().add(new GPIOOutput(
                "pin_33",
                33,
                GPIOResistorMode.NONE,
                GPIOPinType.DIGITAL,
                program.fromTable("invert")
        ));

        program.write(new BitWriter(new FileOutputStream(args[0])));
    }
}
