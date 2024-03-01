package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;
import java.util.List;

public class Table extends AbstractValue implements Writable {
    private final String name;
    private final TableType type;
    private final List<Dimension> dimensions;
    private final float[] data;

    public Table(String name, TableType type, List<Dimension> dimensions, float[] data) {
        super(name, dimensions.stream().allMatch(d -> d.getSource().isStatic()));

        this.name = name;
        this.type = type;
        this.dimensions = dimensions;
        this.data = data;
    }

    public Table(String name, List<Dimension> dimensions, float[] data) {
        this(name, TableType.ARITHMETIC, dimensions, data);
    }

    @Override
    public float get() {
        return 0;
    }

    @Override
    public void write(Program program, BitWriter writer) throws IOException {
        writeAscii(writer, name);
        writer.write(type.getFlag() & 0xFF);
        writer.writeShort((short) (dimensions.size() & 0xFFFF));
        for (Dimension d : dimensions) {
            d.write(program, writer);
        }
        writer.writeInt(data.length);
        writeFloatsLE(writer, data);
    }
}
