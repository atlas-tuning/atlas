package com.github.manevolent.atlas.definition;

public class ScalingOperation {

    private ArithmeticOperation operation;
    private float coefficient;

    public static ScalingOperation from(ArithmeticOperation operation, float coefficient) {
        return ScalingOperation.builder().withOperation(operation).withCoefficient(coefficient).build();
    }

    public ArithmeticOperation getOperation() {
        return operation;
    }

    public void setOperation(ArithmeticOperation operation) {
        this.operation = operation;
    }

    public float getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(float coefficient) {
        this.coefficient = coefficient;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ScalingOperation operation = new ScalingOperation();

        public Builder withOperation(ArithmeticOperation operation) {
            this.operation.setOperation(operation);
            return this;
        }

        public Builder withCoefficient(float coefficient) {
            this.operation.setCoefficient(coefficient);
            return this;
        }

        public ScalingOperation build() {
            return operation;
        }
    }
}
