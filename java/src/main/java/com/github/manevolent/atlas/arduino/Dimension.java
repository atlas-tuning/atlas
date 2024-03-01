package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;

public class Dimension implements Writable {
    private final Value source;
    private final SubValue subValue;
    private final Integration integration;
    private final float[] anchors;

    public Dimension(Value source, SubValue subValue, Integration integration, float[] anchors) {
        this.source = source;
        this.subValue = subValue;
        this.integration = integration;
        this.anchors = anchors;
    }

    public Value getSource() {
        return source;
    }

    public Integration getIntegration() {
        return integration;
    }

    public float[] getAnchors() {
        return anchors;
    }

    public float getAnchorAt(int index) {
        return anchors[index];
    }

    public SubValue getSubValue() {
        return subValue;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void write(Program program, BitWriter writer) throws IOException {
        int sourceIndex;
        if (program.getInputs().contains(source)) {
            sourceIndex = program.getInputs().indexOf(source);
            writer.writeShort((short) (sourceIndex & 0xFFFF));
            writer.write(getSubValue().getCode() & 0xFF);
        } else if (program.getTables().contains(source)) {
            sourceIndex = program.getTables().indexOf(source);
            sourceIndex |= 0x8000; // Set high bit
            writer.writeShort((short) (sourceIndex & 0xFFFF));
        } else {
            throw new IllegalArgumentException("Failed to find source for dimension");
        }

        int num_cols = getAnchors().length;
        int integrationIndex = getIntegration().getFlag();
        byte flag = (byte) (((num_cols & 0x3F) | ((integrationIndex & 0x3) << 6)) & 0xFF);
        writer.write(flag);

        writeFloatsLE(writer, getAnchors());
    }
}
