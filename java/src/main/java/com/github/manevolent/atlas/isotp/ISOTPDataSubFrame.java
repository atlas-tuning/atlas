package com.github.manevolent.atlas.isotp;

public abstract class ISOTPDataSubFrame extends ISOTPSubFrame {

    @Override
    public abstract byte[] getData();

    public abstract void setData(byte[] data);

}
