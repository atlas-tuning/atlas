package com.github.manevolent.atlas;

import com.github.manevolent.atlas.uds.AsyncUDSSession;
import com.github.manevolent.atlas.uds.UDSFrame;
import com.github.manevolent.atlas.uds.request.UDSReadDataByIDRequest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UDSTest {

    @Test
    public void testWrite() throws IOException {
        int did = 0x1234;

        TestCanFrameWriter writer = new TestCanFrameWriter();
        TestCanDevice testDevice = new TestCanDevice(writer);
        AsyncUDSSession session = new AsyncUDSSession(testDevice);
        session.request(new UDSReadDataByIDRequest(new int[] { did }));

        byte[] written = writer.getWritten();

        TestCanFrameReader reader = new TestCanFrameReader(new ByteArrayInputStream(written));
        testDevice = new TestCanDevice(reader);
        session = new AsyncUDSSession(testDevice);
        UDSFrame read = session.reader().read();

        assertEquals(read.getBody().getClass(), UDSReadDataByIDRequest.class);
        assertArrayEquals(((UDSReadDataByIDRequest)read.getBody()).getDids(), new int[] { did });
    }

}
