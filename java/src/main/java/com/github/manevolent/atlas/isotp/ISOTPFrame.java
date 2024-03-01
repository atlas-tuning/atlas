package com.github.manevolent.atlas.isotp;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.Addressed;
import com.github.manevolent.atlas.Frame;

public class ISOTPFrame implements Frame, Addressed {
    private final Address address;
    private final byte[] reassembled;

    public ISOTPFrame(Address address, byte[] reassembled) {
        this.address = address;
        this.reassembled = reassembled;
    }

    @Override
    public byte[] getData() {
        return reassembled;
    }

    @Override
    public String toString() {
        return toHexString();
    }

    @Override
    public Address getAddress() {
        return address;
    }
}
