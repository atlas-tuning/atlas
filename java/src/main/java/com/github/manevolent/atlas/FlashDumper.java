package com.github.manevolent.atlas;

import com.github.manevolent.atlas.protocol.isotp.ISOTPFrameReader;
import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.protocol.subaru.SubaruProtocols;
import com.github.manevolent.atlas.protocol.uds.UDSFrame;
import com.github.manevolent.atlas.protocol.uds.UDSFrameReader;
import com.github.manevolent.atlas.protocol.uds.request.UDSDefineDataIdentifierRequest;
import com.github.manevolent.atlas.protocol.uds.request.UDSTransferRequest;
import com.github.manevolent.atlas.protocol.uds.response.UDSReadDataByIDResponse;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;

public class FlashDumper {

    public static void main(String[] args) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(args[0], "rw");

        SerialTatrixOpenPortFactory canDeviceFactory =
                new SerialTatrixOpenPortFactory(SerialTactrixOpenPort.CommunicationMode.DIRECT_SOCKET);

        Collection<J2534DeviceDescriptor> devices = canDeviceFactory.findDevices();
        J2534DeviceDescriptor deviceDescriptor = devices.stream().findFirst().orElseThrow(() ->
                new IllegalArgumentException("No can devices found"));
        J2534Device device = deviceDescriptor.createDevice();

        ISOTPFrameReader isotpReader = new ISOTPFrameReader(device.openCAN().reader());
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

            if (frame.getBody() instanceof UDSDefineDataIdentifierRequest ||
                frame.getBody() instanceof UDSReadDataByIDResponse) {
                System.out.println(frame.toString());
            }

            if (frame.getBody() instanceof UDSTransferRequest) {
                UDSTransferRequest transferRequest = (UDSTransferRequest) frame.getBody();
                raf.seek(transferRequest.getAddress());
                raf.write(transferRequest.getData());
            }
        }

        raf.close();
    }

}
