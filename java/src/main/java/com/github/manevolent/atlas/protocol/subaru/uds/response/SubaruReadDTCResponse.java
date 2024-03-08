package com.github.manevolent.atlas.protocol.subaru.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SubaruReadDTCResponse extends UDSResponse {
    private Set<Short> dtcs;

    @Override
    public void read(BitReader reader) throws IOException {
        int num = reader.readByte();
        dtcs = new HashSet<>(num);
        for (int n = 0; n < num; n++) {
            dtcs.add(reader.readShort());
        }
    }

    public Set<Short> getDtcs() {
        return dtcs;
    }

    @Override
    public String toString() {
        return String.format("dtcs=%s",
                dtcs.stream().map(s -> Integer.toHexString(s & 0xffff)).collect(Collectors.joining(",")));
    }

}
