package com.github.manevolent.atlas.uds;

public enum RoutineControlSubFunction {
    START_ROUTINE(0x1),
    STOP_ROUTINE(0x2),
    REQUEST_ROUTINE_RESULTS(0x3);

    private int code;

    RoutineControlSubFunction(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
