package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.j2534.ISOTPDevice;
import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.uds.response.UDSNegativeResponse;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.manevolent.atlas.uds.NegativeResponseCode.RESPONSE_PENDING;

public class AsyncUDSSession extends Thread implements UDSSession {
    private final ISOTPDevice device;
    private final UDSProtocol protocol;

    @SuppressWarnings("rawtypes")
    private final Map<Integer, UDSTransaction> activeTransactions = new HashMap<>();

    private UDSFrameReader reader;
    private UDSFrameWriter writer;

    public AsyncUDSSession(ISOTPDevice device, UDSProtocol protocol) {
        this.device = device;
        this.protocol = protocol;
    }

    public AsyncUDSSession(ISOTPDevice device) {
        this(device, UDSProtocol.STANDARD);
    }

    private void ensureInitialized() throws IOException {
        synchronized (this) {
            if (this.reader == null || this.writer == null) {

                this.reader = new UDSFrameReader(device.reader(), protocol);
                this.writer = new UDSFrameWriter(device.writer(), protocol);
            }
        }
    }

    public UDSFrameReader reader() throws IOException {
        ensureInitialized();
        return reader;
    }

    public UDSFrameWriter writer() throws IOException {
        ensureInitialized();
        return writer;
    }

    @Override
    public void run() {
        try {
            handle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected long handle() throws IOException {
        for (long n = 0;;n++) {
            try {
                handleNext();
            } catch (EOFException ex) {
                // silently exit
                return n;
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected UDSResponse handleNext() throws IOException {
        UDSFrame frame = reader().read();
        if (frame == null) {
            return null;
        }

        System.out.println(frame.toString());

        if (frame.getBody() instanceof UDSResponse) {
            if (frame.getBody() instanceof UDSNegativeResponse) {
                UDSNegativeResponse negativeResponse = (UDSNegativeResponse) frame.getBody();
                if (negativeResponse.getResponseCode() == RESPONSE_PENDING) {
                    return null;
                }

                UDSTransaction transaction = activeTransactions.get(negativeResponse.getRejectedSid() & 0xFF);
                if (transaction != null) {
                    transaction.supplyException(negativeResponse);
                }
            } else {
                int responseSid = frame.getServiceId();
                UDSQuery query = protocol.getBySid(responseSid);
                int serviceId = query.getMapping(UDSSide.REQUEST).getSid();
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

    public <T extends UDSResponse> void request(UDSComponent component, UDSRequest<T> request,
                                                Consumer<T> callback, Consumer<Exception> error)
            throws IOException {
        request(component.getSendAddress(), request, callback, error);
    }

    public <T extends UDSResponse> void request(Address destination, UDSRequest<T> request,
                                                             Consumer<T> callback, Consumer<Exception> error)
            throws IOException {
        try (UDSTransaction<T> transaction = request(destination, request)) {
            callback.accept(transaction.get());
        } catch (Exception e) {
            error.accept(e);
        }
    }

    public <T extends UDSResponse> UDSTransaction<T> request(Address destination, UDSRequest<T> request)
            throws IOException {
        System.out.println("Request: " + Frame.toHexString(request.write()));

        final int serviceId = protocol.getSid(request.getClass());

        UDSTransaction<T> transaction = new UDSTransaction<>() {
            @Override
            public void close() {
                AsyncUDSSession.this.activeTransactions.remove((serviceId & 0xFF), this);
            }
        };

        if (this.activeTransactions.putIfAbsent((serviceId & 0xFF), transaction) != null) {
            throw new IllegalStateException("There is an outstanding transaction for SID " + (serviceId & 0xFF));
        }

        writer().write(destination, request);
        return transaction;
    }
}
