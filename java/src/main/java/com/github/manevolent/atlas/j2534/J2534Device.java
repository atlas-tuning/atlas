package com.github.manevolent.atlas.j2534;

import com.github.manevolent.atlas.can.CANArbitrationId;
import com.github.manevolent.atlas.uds.UDSComponent;

import java.io.IOException;
import java.util.Arrays;

public interface J2534Device {

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
    }

}
