package com.github.manevolent.atlas.definition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Scale {
    public static Scale ONE = Scale.builder().build();

    private List<ScalingOperation> operations;
    private Unit unit;
    private DataFormat format;
    private String name;

    public String getName() {
        return name;
    }

    public float forward(float a) {
        for (ScalingOperation operation : operations) {
            a = operation.getOperation().forward(a, operation.getCoefficient());
        }

        return a;
    }

    public float reverse(float a) {
        ListIterator<ScalingOperation> listIterator =
                operations.listIterator(operations.size());

        while (listIterator.hasPrevious()) {
            ScalingOperation operation = listIterator.previous();
            a = operation.getOperation().reverse(a, operation.getCoefficient());
        }

        return a;
    }

    public void setOperations(List<ScalingOperation> operations) {
        this.operations = operations;
    }

    public Unit getUnit() {
        return unit;
    }

    public DataFormat getFormat() {
        return format;
    }

    public static Builder builder() {
        return new Builder();
    }

    public float getPrecision() {
        float a = forward(0);
        float b = forward(1);
        return Math.abs(a - b);
    }

    @Override
    public String toString() {
        String formatString = getFormat().name().toLowerCase() + ", " + getUnit().getText();
        if (name == null) {
            return formatString;
        } else {
            return name + " (" + formatString + ")";
        }
    }

    public static class Builder {
        private final Scale scale = new Scale();

        public Builder() {
            this.scale.setOperations(new ArrayList<>());
        }

        public Builder withName(String name) {
            scale.name = name;
            return this;
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

        public Builder withOperation(ArithmeticOperation operation, int coefficient) {
            return withOperation(ScalingOperation.builder()
                    .withOperation(operation)
                    .withCoefficient((float) coefficient)
                    .build());
        }

        public Builder withOperations(ScalingOperation... operations) {
            scale.operations.addAll(Arrays.asList(operations));
            return this;
        }

        public Builder withOperations(Scale scale) {
            this.scale.operations.addAll(scale.operations);
            return this;
        }

        public Builder withOperations(Scale.Builder builder) {
            this.scale.operations.addAll(builder.scale.operations);
            if (builder.scale.unit != null) {
                withUnit(builder.scale.unit);
            }
            if (builder.scale.format != null && this.scale.format == null) {
                withFormat(builder.scale.format);
            }
            return this;
        }

        public Builder withUnit(Unit unit) {
            this.scale.unit = unit;
            return this;
        }

        public Builder withFormat(DataFormat format) {
            this.scale.format = format;
            return this;
        }

        public Scale build() {
            return scale;
        }
    }
}
