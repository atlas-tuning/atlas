package com.github.manevolent.atlas;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class RandomTest {

    @Test
    public void buffer() {

        double buffer = 0d;
        int n = 0;

        Random random = new Random();
        while (true) {
            double value = random.nextDouble();
            if (buffer == 0) {
                buffer = 0.5;
            }

            buffer += value;
            buffer -= (buffer / 100);
            System.out.println((buffer / 100));
        }

    }
}
