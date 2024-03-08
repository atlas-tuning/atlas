package com.github.manevolent.atlas.protocol.uds;

public enum DiagnosticSessionType {
    DEFAULT_SESSION(0x01),
    PROGRAMMING_SESSION(0x02),
    EXTENDED_SESSION(0x03);

    private int code;
    DiagnosticSessionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}