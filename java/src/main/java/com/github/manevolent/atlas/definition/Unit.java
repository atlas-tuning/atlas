package com.github.manevolent.atlas.definition;

import java.util.function.Supplier;

public enum Unit {
    CELSIUS(() -> UnitClass.TEMPERATURE, "\u00B0C"),
    FAHRENHEIT(() -> UnitClass.TEMPERATURE, "\u00B0F"),
    DEGREES(() -> UnitClass.NONE, "\u00B0"),
    RPM(() -> UnitClass.NONE, "RPM"),
    FT_LB(() -> UnitClass.TORQUE, "ft-lb"),
    NM(() -> UnitClass.TORQUE, "Nm"),
    PERCENT(() -> UnitClass.NONE, "%"),
    PSI(() -> UnitClass.PRESSURE, "psi"),
    KPA(() -> UnitClass.PRESSURE, "kPa"),
    IN_HG(() -> UnitClass.PRESSURE, "inHg"),
    VOLTS(() -> UnitClass.NONE, "V"),
    HZ(() -> UnitClass.NONE, "Hz"),
    G_PER_REV(() -> UnitClass.NONE, "g/rev"),
    STEPS(() -> UnitClass.NONE, "steps"),

    METER(() -> UnitClass.DISTANCE, "m"),
    MILLIMETER(() -> UnitClass.DISTANCE, "mm");

    private final Supplier<UnitClass> unitClass;
    private final String text;

    Unit(Supplier<UnitClass> unitClass, String text) {
        this.unitClass = unitClass;
        this.text = text;
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

    public String getText() {
        return text;
    }


    @Override
    public String toString() {
        return getText();
    }
}
