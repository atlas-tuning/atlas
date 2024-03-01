package com.github.manevolent.atlas.definition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scale {
    public static Scale ONE = Scale.builder().build();

    private List<ScalingOperation> operations;

    public float forward(float a) {
        for (ScalingOperation operation : operations) {
            a = operation.getOperation().forward(a, operation.getCoefficient());
        }

        return a;
    }

    public float reverse(float a) {
        for (ScalingOperation operation : operations) {
            a = operation.getOperation().reverse(a, operation.getCoefficient());
        }

        return a;
    }

    public void setOperations(List<ScalingOperation> operations) {
        this.operations = operations;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Scale scale = new Scale();

        public Builder() {
            this.scale.setOperations(new ArrayList<>());
        }

        public Builder withOperation(ScalingOperation operation) {
            scale.operations.add(operation);
            return this;
        }

        public Builder withOperation(ArithmeticOperation operation, float coefficient) {
           return withOperation(ScalingOperation.builder()
                   .withOperation(operation)
                   .withCoefficient(coefficient)
                   .build());
        }

        public Builder withOperations(ScalingOperation... operations) {
            scale.operations.addAll(Arrays.asList(operations));
            return this;
        }

        public Scale build() {
            return scale;
        }
    }
}
