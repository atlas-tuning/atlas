package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CanDevice;
import com.github.manevolent.atlas.can.CanDeviceDescriptor;
import com.github.manevolent.atlas.can.CanFrameReader;
import com.github.manevolent.atlas.can.OpenPort2FrameReader;
import com.github.manevolent.atlas.can.serial.SerialCanDeviceFactory;
import com.github.manevolent.atlas.can.serial.SerialTactrixOpenPort;
import com.github.manevolent.atlas.isotp.ISOTPFrameReader;
import com.github.manevolent.atlas.subaru.SubaruDITCommands;
import com.github.manevolent.atlas.subaru.SubaruDITComponent;
import com.github.manevolent.atlas.subaru.SubaruProtocols;
import com.github.manevolent.atlas.subaru.uds.request.Subaru4Request;
import com.github.manevolent.atlas.subaru.uds.request.SubaruReadDTCRequest;
import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus7Request;
import com.github.manevolent.atlas.uds.*;
import com.github.manevolent.atlas.uds.request.UDSClearDTCInformationRequest;
import com.github.manevolent.atlas.uds.response.UDSClearDTCInformationResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;

public class Main {

    public static void main(String[] args) throws Exception {
        SerialCanDeviceFactory canDeviceFactory =
                new SerialCanDeviceFactory(SerialTactrixOpenPort.CommunicationMode.DIRECT_SOCKET);

        Collection<CanDeviceDescriptor> devices = canDeviceFactory.findDevices();
        CanDeviceDescriptor deviceDescriptor = devices.stream().findFirst().orElseThrow(() ->
                new IllegalArgumentException("No can devices found"));
        CanDevice device = deviceDescriptor.createDevice();
        UDSProtocol protocol = SubaruProtocols.DIT;
        AsyncUDSSession session = new AsyncUDSSession(device, protocol);
        session.start();
    }


    public static void main2(String[] args) throws IOException {
        String path = args[0];
        File file = new File(path);

        RandomAccessFile extracted = new RandomAccessFile("out.bin", "rw");

        CanFrameReader canReader = new OpenPort2FrameReader(new FileInputStream(file));
        ISOTPFrameReader isotpReader = new ISOTPFrameReader(canReader);
        UDSFrameReader udsReader = new UDSFrameReader(isotpReader, SubaruProtocols.DIT);
        UDSFrame frame;
        while (true) {
            try {
                frame = udsReader.read();
            } catch (IOException ex) {
                ex.printStackTrace();
                continue;
            }

            if (frame == null) {
                break;
            }

            System.out.println(frame.toString());
        }
    }


}
