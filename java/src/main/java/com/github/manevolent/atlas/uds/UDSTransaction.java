package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.uds.response.UDSNegativeResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class UDSTransaction<T extends UDSResponse> implements AutoCloseable {
    private List<T> responses = new ArrayList<>();
    private UDSNegativeResponse exception;

    public UDSTransaction() {
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

    public T get() throws IOException, InterruptedException {
        synchronized (this) {
            while (this.responses.isEmpty() && exception == null) {
                this.wait();
            }

            if (exception != null) {
                throw new IOException(exception.getResponseCode().name());
            }

            return this.responses.remove(0);
        }
    }
}
