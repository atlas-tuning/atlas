package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CANFrame;
import com.github.manevolent.atlas.isotp.ISOTPFrame;
import com.github.manevolent.atlas.isotp.ISOTPFrameReader;
import com.github.manevolent.atlas.isotp.ISOTPFrameWriter;
import com.github.manevolent.atlas.j2534.CANDevice;
import com.github.manevolent.atlas.j2534.ISOTPDevice;
import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.can.CANFrameReader;
import com.github.manevolent.atlas.can.CANFrameWriter;

import java.io.IOException;

public class TestJ2534Device implements J2534Device {
    private final CANFrameReader reader;
    private final CANFrameWriter writer;

    public TestJ2534Device(CANFrameReader reader, CANFrameWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public TestJ2534Device(CANFrameReader reader) {
        this.reader = reader;
        this.writer = null;
    }

    public TestJ2534Device(CANFrameWriter writer) {
        this.reader = null;
        this.writer = writer;
    }

    @Override
    public CANDevice openCAN() throws IOException {
        return openCAN(new CANFilter[0]);
    }

    @Override
    public CANDevice openCAN(CANFilter... filters) throws IOException {
        return new CANDevice() {
            @Override
            public FrameReader<CANFrame> reader() {
                return reader;
            }

            @Override
            public FrameWriter<CANFrame> writer() {
                return writer;
            }

            @Override
            public void close() throws Exception {

            }
        };
    }

    @Override
    public ISOTPDevice openISOTOP(ISOTPFilter... filters) throws IOException {
        return new ISOTPDevice() {
            @Override
            public FrameReader<ISOTPFrame> reader() {
                return new ISOTPFrameReader(reader);
            }

            @Override
            public FrameWriter<BasicFrame> writer() {
                return new ISOTPFrameWriter(writer);
            }

            @Override
            public void close() throws Exception {

            }
        };
    }
}
