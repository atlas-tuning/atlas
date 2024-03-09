package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.model.MemoryParameter;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;

public class MemoryFrame {
    private final Instant instant;
    private final LinkedHashMap<MemoryParameter, byte[]> data;

    public MemoryFrame(Instant instant) {
        this.instant = instant;
        this.data = new LinkedHashMap<>();
    }

    public MemoryFrame() {
        this(Instant.now());
    }

    public void setData(MemoryParameter parameter, byte[] data) {
        this.data.put(parameter, data);
    }

    public Collection<MemoryParameter> getParameters() {
        return data.keySet();
    }

    public byte[] getData(MemoryParameter parameter) {
        return data.get(parameter);
    }

    public Float getValue(MemoryParameter parameter) {
        byte[] data = getData(parameter);
        if (data == null || data.length == 0) {
            return null;
        }
        return parameter.getValue(data);
    }

    public Instant getInstant() {
        return instant;
    }
}
