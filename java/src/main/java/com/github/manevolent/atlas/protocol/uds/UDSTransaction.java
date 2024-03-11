package com.github.manevolent.atlas.protocol.uds;

import com.github.manevolent.atlas.protocol.uds.response.UDSNegativeResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public abstract class UDSTransaction<Q extends UDSRequest<T>, T extends UDSResponse> implements AutoCloseable {
    public static final int TIMEOUT_MILLIS = 2_000;

    private final Class<Q> requestClass;
    private final boolean responseExpected;
    private List<T> responses = new ArrayList<>();
    private UDSNegativeResponse exception;

    public UDSTransaction(Class<Q> requestClass, boolean responseExpected) {
        this.requestClass = requestClass;
        this.responseExpected = responseExpected;
    }

    public void supply(T response) {
        synchronized (this) {
            this.responses.add(response);
            this.notify();
        }
    }

    public void supplyException(UDSNegativeResponse exception) {
        synchronized (this) {
            this.exception = exception;
            this.notifyAll();
        }
    }

    public T get() throws IOException, InterruptedException, TimeoutException {
        if (!responseExpected) {
            return null;
        }

        synchronized (this) {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < TIMEOUT_MILLIS &&
                    this.responses.isEmpty() && exception == null) {
                this.wait(TIMEOUT_MILLIS);
            }

            if (System.currentTimeMillis() - start >= TIMEOUT_MILLIS) {
                throw new TimeoutException("Timeout waiting for response to "
                        + requestClass.getSimpleName());
            }

            if (exception != null) {
                if (exception.getResponseCode() == NegativeResponseCode.SECURITY_ACCESS_DENIED) {
                    throw new SecurityException(exception.getResponseCode().name());
                } else if (exception.getResponseCode() == NegativeResponseCode.INVALID_KEY) {
                    throw new SecurityException(exception.getResponseCode().name());
                }
                throw new IOException(exception.getResponseCode().name());
            }

            return this.responses.remove(0);
        }
    }

    public void join() throws TimeoutException {
        try {
            get();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
