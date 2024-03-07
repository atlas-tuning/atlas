package com.github.manevolent.atlas;

import com.github.manevolent.atlas.model.ArithmeticOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArithmeticOperationTest {
    @Test
    public void testBidirectional() {
        final float testValue = 37;
        final float testCoefficient = 8;
        for (ArithmeticOperation op : ArithmeticOperation.values()) {
            float forward = op.forward(testValue, testCoefficient);
            float reverse = op.reverse(forward, testCoefficient);
            assertEquals(reverse, testValue, 0.01f, op.name());
        }
    }
}
