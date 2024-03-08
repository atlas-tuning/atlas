package com.github.manevolent.atlas.protocol.uds;

public final class UDSSide<T extends UDSBody> {
    public static UDSSide<UDSRequest<?>> REQUEST = new UDSSide<>("Request");
    public static UDSSide<UDSResponse> RESPONSE = new UDSSide<>("Response");

    private final String name;

    public UDSSide(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
