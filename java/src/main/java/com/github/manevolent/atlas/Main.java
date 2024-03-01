package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CanDevice;
import com.github.manevolent.atlas.can.CanDeviceDescriptor;
import com.github.manevolent.atlas.can.serial.SerialCanDeviceFactory;
import com.github.manevolent.atlas.can.serial.SerialTactrixOpenPort;
import com.github.manevolent.atlas.uds.AsyncUDSSession;
import com.github.manevolent.atlas.uds.request.UDSReadDataByIDRequest;

import java.io.IOException;
import java.util.Collection;

public class Main {

    public static void main(String[] args) throws Exception {
        SerialCanDeviceFactory canDeviceFactory =
                new SerialCanDeviceFactory(SerialTactrixOpenPort.CommunicationMode.DIRECT_SOCKET);

        Collection<CanDeviceDescriptor> devices = canDeviceFactory.findDevices();
        CanDeviceDescriptor deviceDescriptor = devices.stream().findFirst().orElseThrow(() ->
                new IllegalArgumentException("No can devices found"));
        CanDevice device = deviceDescriptor.createDevice();
        AsyncUDSSession session = new AsyncUDSSession(device);
        session.start();

        try (var transaction = session.request(new UDSReadDataByIDRequest(new int[] { 0x1010 }))) {
            System.out.println(transaction.get().toHexString());
        }
    }

}
