package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
                "clutch",
                32,
                GPIOResistorMode.PULL_DOWN,
                GPIOPinType.DIGITAL,
                null,
                null
        ));

        program.getInputs().add(new GPIOInput(
                "brake",
                35,
                GPIOResistorMode.PULL_DOWN,
                GPIOPinType.DIGITAL,
                null,
                null
        ));

        program.getTables().add(new Table(
                "clutch_and_brake_pressed",
                TableType.ARITHMETIC,
                Arrays.asList(new Dimension(
                        program.fromInput("clutch"),
                        GPIOInput.SubValue.NONE,
                        Integration.FLOOR,
                        new float[]{0f, 1f}
                ),  new Dimension(
                        program.fromInput("brake"),
                        GPIOInput.SubValue.NONE,
                        Integration.FLOOR,
                        new float[]{0f, 1f}
                )),
                new float[] { 0.0f, 0.0f,
                              0.0f, 1.0f }
        ));

        program.getTables().add(new Table(
                "accelerator_value",
                TableType.ARITHMETIC,
                Arrays.asList(new Dimension(
                        program.fromInput("clutch"),
                        GPIOInput.SubValue.SECONDS_SINCE_LAST_CHANGE,
                        Integration.FLOOR,
                        new float[]{ 0f, 0.25f, 0.50f, 0.75f, 1f }
                ), new Dimension(
                        program.fromTable("clutch_and_brake_pressed"),
                        GPIOInput.SubValue.NONE,
                        Integration.LINEAR,
                        new float[]{ 0f, 1f }
                )),
                new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                              1.0f, 1.0f, 0.0f, 0.0f, 0.0f }
        ));

        program.getOutputs().add(new GPIOOutput(
                "accelerator_pedal",
                33,
                GPIOResistorMode.NONE,
                GPIOPinType.DIGITAL,
                program.fromTable("accelerator_value"),
                null,
                null,
                null
        ));

        program.write(new BitWriter(new FileOutputStream(args[0])));
    }
}
