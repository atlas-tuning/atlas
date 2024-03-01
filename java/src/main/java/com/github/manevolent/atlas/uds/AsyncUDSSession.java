package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.can.CanDevice;
import com.github.manevolent.atlas.isotp.ISOTPFrameReader;
import com.github.manevolent.atlas.isotp.ISOTPFrameWriter;
import com.github.manevolent.atlas.uds.response.UDSNegativeResponse;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AsyncUDSSession extends Thread implements UDSSession {
    private final CanDevice device;
    private UDSFrameReader reader;
    private UDSFrameWriter writer;

    private Map<Integer, UDSTransaction> activeTransactions = new HashMap<>();

    public AsyncUDSSession(CanDevice device) {
        this.device = device;
    }

    private void ensureInitialized() throws IOException {
        synchronized (this) {
            if (this.reader == null || this.writer == null) {
                this.reader = new UDSFrameReader(new ISOTPFrameReader(device.reader()));
                this.writer = new UDSFrameWriter(new ISOTPFrameWriter(device.writer()));
            }
        }
    }

    protected UDSFrameReader reader() throws IOException {
        return reader;
    }

    protected UDSFrameWriter writer() throws IOException {
        return new UDSFrameWriter(new ISOTPFrameWriter(device.writer()));
    }

    @Override
    public void run() {
        try {
            handle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long handle() throws IOException {
        for (long n = 0;;n++) {
            try {
                handleNext();
            } catch (EOFException ex) {
                // silently exit
                return n;
            }
        }
    }

    public UDSResponse handleNext() throws IOException {
        UDSFrame frame = reader().read();
        if (frame.getBody() instanceof UDSResponse) {
            if (frame.getBody() instanceof UDSNegativeResponse) {
                UDSNegativeResponse negativeResponse = (UDSNegativeResponse) frame.getBody();
                UDSTransaction transaction = activeTransactions.get(negativeResponse.getRejectedSid() & 0xFF);
                if (transaction != null) {
                    transaction.supplyException(negativeResponse);
                }
            } else {
                byte serviceId = frame.getBody().getType().getRequestSid();
                UDSTransaction transaction = activeTransactions.get(serviceId & 0xFF);
                if (transaction != null) {
                    transaction.supply((UDSResponse) frame.getBody());
                }
            }

            return (UDSResponse) frame.getBody();
        } else {
            // We're not expecting requests/etc.
            return null;
        }
    }

    public UDSTransaction request(UDSRequest request) throws IOException {
        final byte serviceId = request.getServiceId();
        UDSTransaction transaction = new UDSTransaction() {
            @Override
            public void close() {
                AsyncUDSSession.this.activeTransactions.remove((serviceId & 0xFF), this);
            }
        };
        if (this.activeTransactions.putIfAbsent((int)(serviceId & 0xFF), transaction) != null) {
            throw new IllegalStateException("There is an outstanding transaction for SID " + (serviceId & 0xFF));
        }

        ensureInitialized();
        writer.write(new UDSFrame(request));
        return transaction;
    }
}
