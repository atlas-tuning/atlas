package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.uds.response.UDSNegativeResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class UDSTransaction implements AutoCloseable {
    private List<UDSResponse> responses = new ArrayList<>();
    private UDSNegativeResponse exception;

    public UDSTransaction() {
    }

    public void supply(UDSResponse response) {
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

    public UDSResponse get() throws IOException, InterruptedException {
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
