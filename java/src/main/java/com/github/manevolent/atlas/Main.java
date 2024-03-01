package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CanFrame;
import com.github.manevolent.atlas.can.CanFrameReader;
import com.github.manevolent.atlas.can.OpenPort2FrameReader;
import com.github.manevolent.atlas.isotp.ISOTPFrame;
import com.github.manevolent.atlas.isotp.ISOTPFrameReader;
import com.github.manevolent.atlas.isotp.ISOTPWireFrame;
import com.github.manevolent.atlas.can.serial.SerialCanDeviceFactory;
import com.github.manevolent.atlas.uds.UDSFrame;
import com.github.manevolent.atlas.uds.UDSFrameReader;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.request.UDSTransferRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {

    public static void main_old(String[] args) {
        var factory = new SerialCanDeviceFactory();
        var devices = factory.findDevices();
        var device = devices.stream().findFirst().orElseThrow();
        var canDevice = device.createDevice();
    }

    public static void main(String[] args) throws IOException {
        String path = "../test_install_flash.bin";
        File file = new File(path);

        RandomAccessFile extracted = new RandomAccessFile("tuned_matt_e60.bin", "rw");

        CanFrameReader canReader = new OpenPort2FrameReader(new FileInputStream(file));
        ISOTPFrameReader isotpReader = new ISOTPFrameReader(canReader);
        UDSFrameReader udsReader = new UDSFrameReader(isotpReader);
        UDSFrame frame;
        int readPackets = 0;

        while ((frame = udsReader.read()) != null) {
            if (frame.getType() == null) {
                System.out.println(frame.toString());
            } else if (frame.getType() == UDSFrameType.SECURITY_ACCESS)
                System.out.println(frame.toString());
            readPackets ++;

            if (frame.getBody() instanceof UDSTransferRequest) {
                UDSTransferRequest transferRequest = (UDSTransferRequest) frame.getBody();
                extracted.seek(transferRequest.getAddress());
                extracted.write(transferRequest.getData());
            }
        }
        System.out.println("Read " + readPackets + " UDS packets");
    }

}
