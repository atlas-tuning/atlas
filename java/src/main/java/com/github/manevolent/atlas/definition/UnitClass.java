package com.github.manevolent.atlas.definition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public enum UnitClass {

    NONE(),
    TEMPERATURE(
            () -> Unit.CELSIUS,
            conversions().with(Unit.FAHRENHEIT, (value) -> (value - 32f) * 5f / 9f),
            conversions().with(Unit.FAHRENHEIT, (value) -> (value * 9f / 5f) + 32f)
    ),
    TORQUE(() -> Unit.NM),
    PRESSURE(() -> Unit.IN_HG);

    private final Supplier<Unit> commonUnit;
    private final Map<Unit, Function<Float, Float>> conversionsToCommon;
    private final Map<Unit, Function<Float, Float>> conversionsFromCommon;

    UnitClass(Supplier<Unit> commonUnit,
              Map<Unit, Function<Float, Float>> conversionsToCommon,
              Map<Unit, Function<Float, Float>> conversionsFromCommon) {
        this.commonUnit = commonUnit;
        this.conversionsFromCommon = conversionsFromCommon;
        this.conversionsToCommon = conversionsToCommon;
    }

    UnitClass(Supplier<Unit> commonUnit, ConversionBuilder toCommon, ConversionBuilder fromCommon) {
        this(commonUnit, toCommon.build(), fromCommon.build());
    }

    UnitClass(Supplier<Unit> commonUnit) {
        this(commonUnit, conversions(), conversions());
    }

    UnitClass() {
        this.commonUnit = null;
        this.conversionsToCommon = null;
        this.conversionsFromCommon = null;
    }

    public float convert(Unit source, float value, Unit target) {
        if (source == target) {
            return value;
        }

        if (source.getUnitClass() != target.getUnitClass()) {
            throw new UnsupportedOperationException("no common unit class");
        }

        if (this.commonUnit == null) {
            throw new UnsupportedOperationException("no common unit");
        }

        Unit commonUnit = this.commonUnit.get();

        float common = source == commonUnit ? value :
                this.conversionsToCommon.get(source).apply(value);

        if (target == commonUnit) {
            return common;
        } else {
            return this.conversionsFromCommon.get(target).apply(common);
        }
    }

    private static ConversionBuilder conversions() {
        return new ConversionBuilder();
    }

    public static class ConversionBuilder {
        private final Map<Unit, Function<Float, Float>> conversions;

        public ConversionBuilder() {
            conversions = new HashMap<>();
        }

        public ConversionBuilder with(Unit unit, Function<Float, Float> function) {
            this.conversions.put(unit, function);
            return this;
        }

        public Map<Unit, Function<Float, Float>> build() {
            return conversions;
        }
    }

}
