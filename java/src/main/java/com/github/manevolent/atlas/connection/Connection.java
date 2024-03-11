package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.model.MemoryAddress;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.protocol.uds.ECUResetMode;
import com.github.manevolent.atlas.protocol.uds.UDSSession;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public interface Connection {

    boolean isConnected();

    ConnectionMode getConnectionMode();

    void changeConnectionMode(ConnectionMode newMode) throws IOException, TimeoutException;

    MemoryFrame readFrame(Collection<MemoryParameter> parameters);

    UDSSession getSession() throws IOException, TimeoutException;

    UDSSession connect() throws IOException, TimeoutException;

    void clearDTC() throws IOException, TimeoutException;

    /**
     * Reads stored DTC
     * @return stored DTC
     */
    List<Integer> readDTC() throws IOException, TimeoutException;

    /**
     * Gets the maximum memory read size for this connection.
     * @return maximum memory read size, in bytes.
     */
    default int getMaximumReadSize() {
        return 0xFF;
    }

    /**
     * Reads a block of memory
     * @param address address to start the memory read from
     * @param length number of bytes to read
     * @return bytes read
     */
    byte[] readMemory(MemoryAddress address, int length) throws IOException, TimeoutException;

    /**
     * Reads a block of memory using the maximum memory read size
     * @param address address to start the memory read from
     * @return bytes read
     */
    default byte[] readMemory(MemoryAddress address) throws IOException, TimeoutException {
        return readMemory(address, getMaximumReadSize());
    }

    /**
     * Resets the ECU
     * @param mode reset mode to perform.
     */
    void resetECU(ECUResetMode mode) throws IOException, TimeoutException;

    /**
     * Reads a single DID from the ECU
     * @param did DID to read
     * @return data from the did
     */
    byte[] readDID(short did) throws IOException, TimeoutException;

}
