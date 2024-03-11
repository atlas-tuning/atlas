package com.github.manevolent.atlas;

import com.github.manevolent.atlas.connection.ConnectionMode;
import com.github.manevolent.atlas.connection.SubaruDIConnection;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.model.builtin.SubaruWRX2022MT;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * A lot of these tests require an active connection to a WRX.
 * Therefore, they are ignored.
 */
public class SubaruDIConnectionTest {

    @Disabled
    @Test
    public void test_ReadMemory() throws IOException, TimeoutException {
        Project project = SubaruWRX2022MT.newRom();
        SubaruDIConnection connection = new SubaruDIConnection(project);
        connection.changeConnectionMode(ConnectionMode.READ_MEMORY);
    }

}
