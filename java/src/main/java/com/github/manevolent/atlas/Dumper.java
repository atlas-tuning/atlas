package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CANArbitrationId;
import com.github.manevolent.atlas.can.CANFrameReader;
import com.github.manevolent.atlas.isotp.ISOTPFrameReader;
import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.j2534.tactrix.OpenPort2CANFrameReader;
import com.github.manevolent.atlas.j2534.tactrix.SerialTactrixOpenPort;
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

public class Dumper {

    public static void main(String[] args) throws Exception {
        String path = args[0];
        File file = new File(path);

        CANFrameReader canReader = new OpenPort2CANFrameReader(new FileInputStream(file));
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
