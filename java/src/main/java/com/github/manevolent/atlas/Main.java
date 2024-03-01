package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CanDevice;
import com.github.manevolent.atlas.can.CanDeviceDescriptor;
import com.github.manevolent.atlas.can.CanFrameReader;
import com.github.manevolent.atlas.can.OpenPort2FrameReader;
import com.github.manevolent.atlas.can.serial.SerialCanDeviceFactory;
import com.github.manevolent.atlas.can.serial.SerialTactrixOpenPort;
import com.github.manevolent.atlas.isotp.ISOTPFrame;
import com.github.manevolent.atlas.isotp.ISOTPFrameReader;
import com.github.manevolent.atlas.subaru.SubaruDITCommands;
import com.github.manevolent.atlas.subaru.SubaruProtocols;
import com.github.manevolent.atlas.uds.AsyncUDSSession;
import com.github.manevolent.atlas.uds.UDSFrame;
import com.github.manevolent.atlas.uds.UDSFrameReader;
import com.github.manevolent.atlas.uds.UDSProtocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;

public class Main {

    public static void main2(String[] args) throws Exception {
        SerialCanDeviceFactory canDeviceFactory =
                new SerialCanDeviceFactory(SerialTactrixOpenPort.CommunicationMode.DIRECT_SOCKET);

        Collection<CanDeviceDescriptor> devices = canDeviceFactory.findDevices();
        CanDeviceDescriptor deviceDescriptor = devices.stream().findFirst().orElseThrow(() ->
                new IllegalArgumentException("No can devices found"));
        CanDevice device = deviceDescriptor.createDevice();
        UDSProtocol protocol = UDSProtocol.STANDARD;
        AsyncUDSSession session = new AsyncUDSSession(device, protocol);
        session.start();

        boolean isIgnitionOn = SubaruDITCommands.IGNITION_ON.execute(session);
    }


    public static void main(String[] args) throws IOException {
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
