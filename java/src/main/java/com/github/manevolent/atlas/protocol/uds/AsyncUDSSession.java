package com.github.manevolent.atlas.protocol.uds;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.protocol.isotp.ISOTPSpyDevice;
import com.github.manevolent.atlas.protocol.j2534.ISOTPDevice;
import com.github.manevolent.atlas.protocol.uds.response.UDSNegativeResponse;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AsyncUDSSession extends AbstractUDSSession implements UDSSession {
    private final ISOTPDevice device;
    private final UDSProtocol protocol;

    @SuppressWarnings("rawtypes")
    private final Map<Integer, UDSTransaction> activeTransactions = new HashMap<>();

    private final Reader readThread;
    private UDSFrameReader reader;
    private UDSFrameWriter writer;

    private boolean closed;

    private final Object[] writeLocks = new Object[256];

    public AsyncUDSSession(ISOTPDevice device, UDSProtocol protocol) {
        this.device = device;
        this.protocol = protocol;
        this.readThread = new Reader();

        for (int i = 0; i < writeLocks.length; i ++) {
            writeLocks[i] = new Object();
        }
    }

    public AsyncUDSSession(ISOTPDevice device) {
        this(device, UDSProtocol.STANDARD);
    }

    public void start() {
        if (!this.readThread.isAlive()) {
            this.readThread.start();
        }
    }

    protected void ensureInitialized() {
        synchronized (this) {
            if (this.reader == null || this.writer == null) {
                this.reader = new UDSFrameReader(device.reader(), protocol) {
                    @Override
                    protected void onFrameRead(UDSFrame frame) {
                        AsyncUDSSession.this.onUDSFrameRead(frame);
                    }
                };

                this.writer = new UDSFrameWriter(device.writer(), protocol) {
                    @Override
                    protected void onFrameWrite(UDSFrame frame) {
                        AsyncUDSSession.this.onUDSFrameWrite(frame);
                    }
                };
            }
        }
    }

    public FrameReader<UDSFrame> reader() throws IOException {
        ensureInitialized();
        return reader;
    }

    public FrameWriter<UDSBody> writer() throws IOException {
        ensureInitialized();
        return writer;
    }

    protected long handle() throws IOException {
        long n;
        for (n = 0; !closed; n++) {
            try {
                handleNext();
            } catch (EOFException | SocketTimeoutException ex) {
                // silently exit
            }
        }
        return n;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected UDSResponse handleNext() throws IOException {
        UDSFrame frame = reader().read();
        if (frame == null) {
            return null;
        }

        if (frame.getBody() instanceof UDSResponse) {
            if (frame.getBody() instanceof UDSNegativeResponse) {
                UDSNegativeResponse negativeResponse = (UDSNegativeResponse) frame.getBody();
                if (negativeResponse.getResponseCode() == NegativeResponseCode.RESPONSE_PENDING) {
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

    public <Q extends UDSRequest<T>, T extends UDSResponse>
    void requestAndWait(UDSComponent component, Q request)
            throws IOException, TimeoutException {
        UDSTransaction<Q, T> transaction = request(component.getSendAddress(), request);
        transaction.join();
    }

    public <T extends UDSResponse> void request(UDSComponent component, UDSRequest<T> request)
            throws IOException {
        request(component, request, (response) -> { /*ignored*/ });
    }

    public <T extends UDSResponse> void request(UDSComponent component, UDSRequest<T> request,
                                                Consumer<T> callback)
            throws IOException {
        AtomicReference<Throwable> error = new AtomicReference<>();
        request(component.getSendAddress(), request, callback, error::set);
        Throwable throwable = error.getAcquire();
        if (throwable != null) {
            throw new IOException(throwable);
        }
    }

    public <T extends UDSResponse> void request(UDSComponent component, UDSRequest<T> request,
                                                Consumer<T> callback, Consumer<Exception> error)
            throws IOException {
        request(component.getSendAddress(), request, callback, error);
    }

    public <Q extends UDSRequest<T>, T extends UDSResponse> void request(Address destination, Q request,
                                                             Consumer<T> callback, Consumer<Exception> error)
            throws IOException {
        try (UDSTransaction<Q, T> transaction = request(destination, request)) {
            callback.accept(transaction.get());
        } catch (Exception e) {
            error.accept(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <Q extends UDSRequest<T>, T extends UDSResponse>
        UDSTransaction<Q, T> request(Address destination, Q request) throws IOException, TimeoutException {
        final int serviceId = protocol.getSid(request.getClass());
        synchronized (writeLocks[serviceId & 0xFF]) {
            UDSTransaction<Q, T> transaction = activeTransactions.get(serviceId & 0xFF);
            if (transaction != null) {
                // Wait for this transaction to complete before submitting another
                transaction.join();
            }

            // Construct the new transaction
            transaction = new UDSTransaction<>((Class<Q>) request.getClass(),
                                                request.isResponseExpected()) {
                @Override
                public void close() {
                    AsyncUDSSession.this.activeTransactions.remove(serviceId & 0xFF, this);
                }
            };

            activeTransactions.put(serviceId & 0xFF, transaction);

            // Write to the bus
            try {
                writer().write(destination, request);
            } catch (IOException ex) {
                // If there is a failure, remove the transaction, otherwise we 'brick' this SID
                try {
                    transaction.close();
                } catch (Exception e) {
                    ex.addSuppressed(e);
                }

                throw ex;
            }

            // Return the transaction and release the lock
            return transaction;
        }
    }

    @Override
    public boolean isSpying() {
        return device instanceof ISOTPSpyDevice;
    }

    @Override
    public void close() throws IOException {
        try {
            closed = true;

            if (readThread != null && readThread.isAlive()) {
                readThread.interrupt();
            }
            if (reader != null) {
                reader.close();
            }
            if (device != null) {
                device.close();
            }

            activeTransactions.clear();

            onDisconnected(this);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private class Reader extends Thread {
        Reader() {
            this.setName("UDSSession/" + device.toString() + "/" + protocol.toString());
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                handle();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
