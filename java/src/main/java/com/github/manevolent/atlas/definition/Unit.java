package com.github.manevolent.atlas.definition;

import java.util.function.Supplier;

public enum Unit {
    CELSIUS(() -> UnitClass.TEMPERATURE),
    FAHRENHEIT(() -> UnitClass.TEMPERATURE),
    DEGREES(() -> UnitClass.NONE),
    RPM(() -> UnitClass.NONE),
    FT_LB(() -> UnitClass.TORQUE),
    NM(() -> UnitClass.TORQUE),
    PERCENT(() -> UnitClass.NONE),
    PSI(() -> UnitClass.PRESSURE),
    IN_HG(() -> UnitClass.PRESSURE),
    VOLTS(() -> UnitClass.NONE),
    HZ(() -> UnitClass.NONE),
    G_PER_REV(() -> UnitClass.NONE),
    STEPS(() -> UnitClass.NONE);

    private final Supplier<UnitClass> unitClass;

    Unit(Supplier<UnitClass> unitClass) {
        this.unitClass = unitClass;
    }

    public UnitClass getUnitClass() {
        return unitClass.get();
    }

    public float convert(float value, Unit target) {
        return getUnitClass().convert(this, value, target);
    }

    public void convert(float[] values, Unit target) {
        for (int i = 0; i < values.length; i ++) {
            values[i] = convert(values[i], target);
        }
    }
}
