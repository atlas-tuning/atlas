package com.github.manevolent.atlas.protocol.uds;

public class UDSMapping<T extends UDSBody> {
    private final UDSSide<T> side;
    private final int sid;
    private final Class<? extends T> bodyClass;

    public UDSMapping(UDSSide<T> side, int sid, Class<? extends T> bodyClass) {
        this.side = side;
        this.sid = sid;
        this.bodyClass = bodyClass;
    }

    public UDSSide<T> getSide() {
        return side;
    }

    public int getSid() {
        return sid;
    }

    public Class<? extends T> getBodyClass() {
        return bodyClass;
    }
}
