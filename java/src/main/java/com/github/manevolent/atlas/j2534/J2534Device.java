package com.github.manevolent.atlas.j2534;

import com.github.manevolent.atlas.can.CANArbitrationId;
import com.github.manevolent.atlas.uds.UDSComponent;

import java.io.IOException;
import java.util.Arrays;

public interface J2534Device {

    CANDevice openCAN() throws IOException;

    CANDevice openCAN(CANFilter... filters) throws IOException;

    default ISOTPDevice openISOTOP() throws IOException {
        return openISOTOP(new ISOTPFilter[0]);
    }

    ISOTPDevice openISOTOP(ISOTPFilter... filters) throws IOException;

    default ISOTPDevice openISOTOP(UDSComponent... components) throws IOException {
        return openISOTOP(Arrays.stream(components).map(UDSComponent::toISOTPFilter)
                        .toArray(J2534Device.ISOTPFilter[]::new));
    }


    class CANFilter {
        private final byte[] mask;
        private final byte[] pattern;

        public CANFilter(byte[] mask, byte[] pattern) {
            this.mask = mask;
            this.pattern = pattern;
        }

        public CANFilter(CANArbitrationId mask, CANArbitrationId pattern) {
            this.mask = mask.getData();
            this.pattern = pattern.getData();
        }

        public byte[] getMask() {
            return mask;
        }

        public byte[] getPattern() {
            return pattern;
        }
    }

    class ISOTPFilter extends CANFilter {
        private final byte[] flow;

        public ISOTPFilter(byte[] mask, byte[] pattern, byte[] flow) {
            super(mask, pattern);
            this.flow = flow;
        }

        public ISOTPFilter(CANArbitrationId mask, CANArbitrationId pattern, CANArbitrationId flow) {
            super(mask, pattern);
            this.flow = flow.getData();
        }

        public byte[] getFlow() {
            return flow;
        }

        public boolean testPattern(CANArbitrationId id) {
            byte[] data = id.getData();
            byte[] mask = getMask();
            byte[] maskResult = new byte[getMask().length];
            for (int i = 0; i < maskResult.length; i ++) {
                maskResult[i] = (byte) ((data[i] & 0xFF) & (mask[i] & 0xFF));
            }

            byte[] pattern = getPattern();
            for (int i = 0; i < pattern.length; i ++) {
                if (pattern[i] != maskResult[i])
                    return false;
            }

            return true;
        }

        public boolean testFlow(CANArbitrationId id) {
            byte[] data = id.getData();
            byte[] mask = getMask();
            byte[] maskResult = new byte[getMask().length];
            for (int i = 0; i < maskResult.length; i ++) {
                maskResult[i] = (byte) ((data[i] & 0xFF) & (mask[i] & 0xFF));
            }

            byte[] flow = getFlow();
            for (int i = 0; i < flow.length; i ++) {
                if (flow[i] != maskResult[i])
                    return false;
            }

            return true;
        }
    }

    public static CANFilter CAN_ALL = new CANFilter(new byte[4], new byte[4]);
    public static ISOTPFilter ISOTP_ALL = new ISOTPFilter(new byte[4], new byte[4], new byte[4]);

}
