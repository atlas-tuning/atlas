package com.github.manevolent.atlas;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.protocol.can.CANFrame;
import com.github.manevolent.atlas.protocol.can.CANFrameReader;
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
import com.github.manevolent.atlas.ssm4.Crypto;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class FlashDumper {

    public static void main(String[] args) throws Exception {
        File file = new File("/Users/matt/Downloads/decoded 2.txt");
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);


        List<byte[]> frames = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("Data Bytes: ")) {
                line = line.substring("Data Bytes: ".length()).replace("0x", "").replace(" ", "");
                frames.add(Crypto.toByteArray(line));
            }
        }

        RandomAccessFile raf = new RandomAccessFile(args[0], "rw");

        CANFrameReader canFrameReader = new CANFrameReader() {
            private int frameIndex;

            @Override
            public CANFrame read() throws IOException {
                return new CANFrame(0x00, frames.get(frameIndex++));
            }

            @Override
            public void close() throws Exception {

            }
        };

        ISOTPFrameReader isotpReader = new ISOTPFrameReader(canFrameReader);
        UDSFrameReader udsReader = new UDSFrameReader(isotpReader, SubaruProtocols.DIT);
        UDSFrame frame;


        long total = 0;
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
            Log.can().setLevel(Level.OFF);

            System.out.println(frame.toString());

            if (frame.getBody() instanceof UDSTransferRequest) {
                UDSTransferRequest transferRequest = (UDSTransferRequest) frame.getBody();
                raf.write(transferRequest.getData(), 0, transferRequest.getLength());
                total += transferRequest.getLength();
            }
        }

        raf.close();
    }

}
