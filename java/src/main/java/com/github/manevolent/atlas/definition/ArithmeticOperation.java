package com.github.manevolent.atlas.definition;

import java.util.function.BiFunction;

public enum ArithmeticOperation {

    EXPONENT((a, x) -> {
        return (float) Math.pow(a, x);
    },(a, x) -> {
        return (float) Math.pow(a, 1f/x);
    }),

    MULTIPLY((a, x) -> {
        return a * x;
    }, (a, x) -> {
        return a / x;
    }),

    DIVIDE((a, x) -> {
        return a / x;
    }, (a, x) -> {
        return a * x;
    }),

    ADD((a, x) -> {
        return a + x;
    }, (a, x) -> {
        return a - x;
    }),

    SUBTRACT((a, x) -> {
        return a - x;
    }, (a, x) -> {
        return a + x;
    }),

    RSHIFT((a, x) -> {
        return a / (float)Math.pow(2, x.intValue());
    }, (a, x) -> {
        return a * (float)Math.pow(2, x.intValue());
    }),

    LSHIFT((a, x) -> {
        return a * (float)Math.pow(2, x.intValue());
    }, (a, x) -> {
        return a / (float)Math.pow(2, x.intValue());
    }),;

    private final BiFunction<Float, Float, Float> forwardOperation;
    private final BiFunction<Float, Float, Float> reverseOperation;

    ArithmeticOperation(BiFunction<Float, Float, Float> forwardOperation,
                        BiFunction<Float, Float, Float> reverseOperation) {
        this.forwardOperation = forwardOperation;
        this.reverseOperation = reverseOperation;
    }

    public float forward(float a, float x) {
        return forwardOperation.apply(a, x);
    }

    public float reverse(float a, float x) {
        return reverseOperation.apply(a, x);
    }

}
