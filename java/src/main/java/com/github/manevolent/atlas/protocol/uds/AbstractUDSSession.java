package com.github.manevolent.atlas.protocol.uds;

import java.util.LinkedList;

public abstract class AbstractUDSSession implements UDSSession, UDSListener {
    private final java.util.List<UDSListener> listeners = new LinkedList<>();

    @Override
    public void addListener(UDSListener listener) {
        synchronized (listeners) {
            this.listeners.add(listener);
        }
    }

    @Override
    public boolean removeListener(UDSListener listener) {
        synchronized (listeners) {
            return this.listeners.remove(listener);
        }
    }

    @Override
    public boolean hasListener(UDSListener listener) {
        synchronized (listeners) {
            return this.listeners.contains(listener);
        }
    }

    @Override
    public void onUDSFrameRead(UDSFrame frame) {
        synchronized (listeners) {
            listeners.forEach(l -> l.onUDSFrameRead(frame));
        }
    }

    @Override
    public void onUDSFrameWrite(UDSFrame frame) {
        synchronized (listeners) {
            listeners.forEach(l -> l.onUDSFrameWrite(frame));
        }
    }

    @Override
    public void onDisconnected(UDSSession session) {
        synchronized (listeners) {
            listeners.forEach(l -> l.onDisconnected(session));
        }
    }
}
