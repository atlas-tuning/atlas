package com.github.manevolent.atlas;

import com.github.manevolent.atlas.connection.ConnectionMode;
import com.github.manevolent.atlas.connection.SubaruDITConnection;
import com.github.manevolent.atlas.model.Rom;
import com.github.manevolent.atlas.model.builtin.SubaruWRX2022MT;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * A lot of these tests require an active connection to a WRX.
 * Therefore, they are ignored.
 */
public class SubaruDITConnectionTest {

    @Disabled
    @Test
    public void test_ReadMemory() throws IOException, TimeoutException {
        Rom rom = SubaruWRX2022MT.newRom();
        SubaruDITConnection connection = new SubaruDITConnection(rom);
        connection.changeConnectionMode(ConnectionMode.READ_MEMORY);
    }

}
