package com.github.manevolent.atlas.model;

public class MemoryReference {
    private final Object owner;
    private final String name;
    private final MemoryAddress address;

    public MemoryReference(Object owner, String name, MemoryAddress address) {
        this.owner = owner;
        this.name = name;
        this.address = address;
    }

    public Object getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public MemoryAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return name + " (" + address.toString() + ")";
    }

    public static MemoryReference of(Table table, Series series) {
        return new MemoryReference(table, "Table " + table.getName(), series.getAddress());
    }

    public static MemoryReference of(MemoryParameter parameter) {
        return new MemoryReference(parameter, "Parameter " + parameter.getName(), parameter.getAddress());
    }
}
