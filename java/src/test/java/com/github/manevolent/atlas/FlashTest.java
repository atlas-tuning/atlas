package com.github.manevolent.atlas;

import com.github.manevolent.atlas.definition.*;
import com.github.manevolent.atlas.definition.subaru.SubaruDITFlashEncryption;
import com.github.manevolent.atlas.definition.zip.StreamedFlashSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.github.manevolent.atlas.definition.Axis.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlashTest {
    private static final String testRomName = "BE1DA813";
    private static final String fileFormat = "%s.bin";
    private static final String testRomResource = "/" + String.format(fileFormat, testRomName);

    private static final Scale.Builder rpm_16bit =
            Scale.builder().withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f);

    private static final Scale.Builder rpm_8bit = Scale.builder()
            .withOperation(ArithmeticOperation.LSHIFT, 8)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f);

    /**
     * Unit is kPa
     */
    private static final Scale.Builder directInjectionFuelPressureScale_16bit = Scale.builder()
            .withOperation(ArithmeticOperation.MULTIPLY, (float)0x7D)
            .withOperation(ArithmeticOperation.RSHIFT, 0xB)
            .withOperation(ArithmeticOperation.MULTIPLY, 10.0f);

    private static final Scale.Builder directInjectionFuelPressureScale_8bit = Scale.builder()
            .withOperation(ArithmeticOperation.MULTIPLY, (float)0x96)
            .withOperation(ArithmeticOperation.MULTIPLY, (float)0x7D)
            .withOperation(ArithmeticOperation.RSHIFT, 0xB)
            .withOperation(ArithmeticOperation.MULTIPLY, 10.0f);

    /**
     * Unit is degrees celsius
     */
    private static final Scale.Builder coolantTemp16BitScale = Scale.builder()
            .withOperation(ArithmeticOperation.MULTIPLY, 5.0f)
            .withOperation(ArithmeticOperation.RSHIFT, 0xB)
            .withOperation(ArithmeticOperation.SUBTRACT, 40.0f);

    private static Table.Builder ignitionTimingBaseTable(FlashRegion code, String name, int timingDataAddress,
                                                         int rpmAddress, int loadAddress) {
        return Table.builder()
                .withName("Base Ignition Timing - " + name)
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, timingDataAddress)
                        .withFormat(DataFormat.UBYTE)
                        .withUnit(Unit.DEGREES)
                        .withScale(Scale.builder().withOperations(
                                ScalingOperation.from(ArithmeticOperation.SUBTRACT, 0x20),
                                ScalingOperation.from(ArithmeticOperation.DIVIDE, 2)
                        )))
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x16)
                        .withFormat(DataFormat.USHORT)
                        .withScale(Scale.builder().withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(0x1E)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(DataFormat.USHORT)
                        .withScale(Scale.builder().withOperation(ArithmeticOperation.MULTIPLY, 0.00006103515625f)));
    }

    private static Table.Builder fuelPressureTargetTable_1D(FlashRegion code,
                                                                String name,
                                                                int dataAddress,
                                                                int coolantTempAddress) {
        return Table.builder()
                .withName("Fuel Pressure Target - " + name)
                .withData(Series.builder()
                        .withName("Fuel Pressure")
                        .withAddress(code, dataAddress)
                        .withFormat(DataFormat.USHORT)
                        .withScale(directInjectionFuelPressureScale_16bit)
                        .withUnit(Unit.KPA)
                )
                .withAxis(X, Series.builder()
                        .withName("Coolant Temperature")
                        .withAddress(code, coolantTempAddress)
                        .withLength(0x10)
                        .withUnit(Unit.CELSIUS)
                        .withFormat(DataFormat.USHORT)
                        .withScale(coolantTemp16BitScale)
                );
    }


    private static Table.Builder fuelPressureTargetTable_2D(FlashRegion code,
                                                            String name,
                                                            int dataAddress,
                                                            boolean _16bit,
                                                            int coolantTempAddress,
                                                            int rpmAddress) {
        return Table.builder()
                .withName("Fuel Pressure Target - " + name)
                .withData(Series.builder()
                        .withName("Fuel Pressure")
                        .withAddress(code, dataAddress)
                        .withFormat(_16bit ? DataFormat.USHORT : DataFormat.UBYTE)
                        .withScale(_16bit ? directInjectionFuelPressureScale_16bit :
                                            directInjectionFuelPressureScale_8bit)
                        .withUnit(Unit.KPA)
                )
                .withAxis(Y, Series.builder()
                        .withName("Coolant Temperature")
                        .withAddress(code, coolantTempAddress)
                        .withLength(0x8)
                        .withUnit(Unit.CELSIUS)
                        .withFormat(DataFormat.USHORT)
                        .withScale(coolantTemp16BitScale)
                )
                .withAxis(X, Series.builder()
                    .withName("RPM")
                    .withAddress(code, rpmAddress)
                    .withLength(0x4)
                    .withUnit(Unit.RPM)
                    .withFormat(DataFormat.UBYTE)
                    .withScale(rpm_8bit)
        );
    }

    private static Table.Builder ignitionTimingGearCompTable(FlashRegion code,
                                                             String gear,
                                                             int dataAddress,
                                                             int rpmAddress,
                                                             int loadAddress) {

        return Table.builder()
                .withName("Ignition Timing Compensation - " + gear + " Gear")
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, dataAddress)
                        .withFormat(DataFormat.UBYTE)
                        .withUnit(Unit.DEGREES)
                )
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x03)
                        .withFormat(DataFormat.USHORT)
                        .withScale(rpm_16bit))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(0x03)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(DataFormat.USHORT)
                        .withScale(Scale.builder().withOperation(ArithmeticOperation.MULTIPLY, 0.00006103515625f)));
    }

    private static Table.Builder[] ignitionTimingIatCompTables(FlashRegion code,
                                                    String name,
                                                    int dataAddress,
                                                    int iatAddress,
                                                    int activationDataAddress,
                                                    int activationRpmAddress,
                                                    int activationCalcLoadAddress) {
        return new Table.Builder[] {
                Table.builder()
                    .withName("Ignition Timing Compensation IAT " + name)
                    .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, dataAddress)
                        .withFormat(DataFormat.UBYTE)
                        .withUnit(Unit.DEGREES)
                    )
                    .withAxis(X, Series.builder()
                        .withName("Air Temperature")
                        .withUnit(Unit.CELSIUS)
                        .withAddress(code, iatAddress)
                        .withLength(0x10)
                        .withFormat(DataFormat.USHORT)
                        .withScale(Scale.builder()
                                .withOperation(ArithmeticOperation.MULTIPLY, 5f)
                                .withOperation(ArithmeticOperation.ADD, 20480f)
                                .withOperation(ArithmeticOperation.DIVIDE, 2048f)
                        )
                ),
                Table.builder()
                    .withName("Ignition Timing Compensation IAT " + name + " Activation")
                    .withData(Series.builder()
                        .withName("Activation")
                        .withAddress(code, activationDataAddress)
                        .withFormat(DataFormat.UBYTE)
                        .withUnit(Unit.PERCENT)
                        .withScale(Scale.builder().withOperation(ArithmeticOperation.DIVIDE, 255f))
                    )
                    .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, activationRpmAddress)
                        .withLength(0x16)
                        .withFormat(DataFormat.USHORT)
                        .withScale(rpm_16bit))
                    .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, activationCalcLoadAddress)
                        .withLength(0x1E)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(DataFormat.USHORT)
                        .withScale(Scale.builder().withOperation(ArithmeticOperation.MULTIPLY, 0.00006103515625f))
                )
        };
    }


    private static Rom newRom() {
        FlashRegion code = new FlashRegion();
        code.setBaseAddress(0x0);
        code.setEncryption(SubaruDITFlashEncryption.WRX_MT_2022_USDM);
        code.setSource(new StreamedFlashSource(() -> FlashTest.class.getResourceAsStream(testRomResource)));
        code.setDataLength(0x003F0000);

        return Rom.builder()
                .withRegion(code)
                .withTable(
                        Table.builder()
                                .withName("Intake Air Temperature Sensor Calibration")
                                .withData(Series.builder()
                                        .withAddress(code, 0x00029780)
                                        .withFormat(DataFormat.UBYTE)
                                        .withScale(Scale.builder().withOperation(ArithmeticOperation.SUBTRACT, 50))
                                        .withUnit(Unit.CELSIUS))
                                .withAxis(X, Series.builder()
                                        .withName("Sensor Voltage")
                                        .withAddress(code, 0x000297c0)
                                        .withFormat(DataFormat.USHORT)
                                        .withScale(Scale.builder().withOperation(ArithmeticOperation.DIVIDE, 13106.25f))
                                        .withLength(32))
                ).withTable(
                        Table.builder()
                                .withName("Manifold Air Temperature Sensor Calibration")
                                .withData(Series.builder()
                                        .withAddress(code, 0x000297a0)
                                        .withFormat(DataFormat.UBYTE)
                                        .withScale(Scale.builder().withOperation(ArithmeticOperation.SUBTRACT, 50))
                                        .withUnit(Unit.CELSIUS))
                                .withAxis(X, Series.builder()
                                        .withName("Sensor Voltage")
                                        .withAddress(code, 0x000297c0)
                                        .withFormat(DataFormat.USHORT)
                                        .withScale(Scale.builder().withOperation(ArithmeticOperation.DIVIDE, 13106.25f))
                                        .withLength(32))
                )
                .withTable(ignitionTimingBaseTable(code, "TGVs Closed - AVCS Disabled", 0x000ad514, 0x000a88b4, 0x000a89bc))
                .withTable(ignitionTimingBaseTable(code, "TGVs Closed - AVCS Enabled", 0x000ad7a8, 0x000a88b4, 0x000a89bc))
                .withTable(ignitionTimingBaseTable(code, "TGVs Open - AVCS Disabled", 0x000ada3c, 0x000a88b4, 0x000a89bc))
                .withTable(ignitionTimingBaseTable(code, "TGVs Open - AVCS Enabled", 0x000adcd0, 0x000a88b4, 0x000a89bc))
                .withTable(ignitionTimingGearCompTable(code, "1st", 0x000a8bf0, 0x000a7478, 0x000a7470))
                .withTable(ignitionTimingGearCompTable(code, "2nd", 0x000a8bfc, 0x000a7478, 0x000a7470))
                .withTable(ignitionTimingGearCompTable(code, "3rd", 0x000a8c08, 0x000a7478, 0x000a7470))
                .withTable(ignitionTimingGearCompTable(code, "4th", 0x000a8c14, 0x000a7478, 0x000a7470))
                .withTable(ignitionTimingGearCompTable(code, "5th", 0x000a8c20, 0x000a7478, 0x000a7470))
                .withTables(ignitionTimingIatCompTables(code, "A", 0x000a80ec, 0x000a80cc,
                        0x000ac830, 0x000a8914, 0x000a8980))
                .withTables(ignitionTimingIatCompTables(code, "B", 0x000a7b5c, 0x000a80cc,
                        0x000acac4, 0x000a8914, 0x000a8980))
                .withTable(fuelPressureTargetTable_1D(code, "Warmup Mode 3A #1", 0x000c85c4, 0x000c84e4))
                .withTable(fuelPressureTargetTable_1D(code, "Warmup Mode 3A #2", 0x000c85a4, 0x000c84e4))
                .withTable(fuelPressureTargetTable_1D(code, "Warmup Mode 1A", 0x000c85e4, 0x000c84e4))
                .withTable(fuelPressureTargetTable_1D(code, "Warmup Mode 1A", 0x000c85e4, 0x000c84e4))
                .withTable(fuelPressureTargetTable_1D(code, "Warmup Mode 1B #1", 0x000c8564, 0x000c84e4))
                .withTable(fuelPressureTargetTable_1D(code, "Warmup Mode 1B #2", 0x000c8504, 0x000c84e4))
                .withTable(fuelPressureTargetTable_1D(code, "Warmup Mode 3B #1", 0x000c8544, 0x000c84e4))
                .withTable(fuelPressureTargetTable_1D(code, "Warmup Mode 3B #2", 0x000c8524, 0x000c84e4))
                .withTable(fuelPressureTargetTable_2D(code, "Warmup Mode 4", 0x000c8654, true, 0x000c83e4, 0x000c82f8))
                .withTable(fuelPressureTargetTable_2D(code, "Main", 0x000c83c4, false, 0x000c83b4, 0x000c82f8))
                .build();
    }

    @Test
    public void test_IntakeAirTemperatureSensorCalibration() throws IOException {
        Table iatSensorCal = newRom().findTableByName("Intake Air Temperature Sensor Calibration");

        float[] x_data = iatSensorCal.getSeries(X).getAll();
        float[] table_data = iatSensorCal.getData().getAll();

        iatSensorCal.getData().getUnit().convert(table_data, Unit.FAHRENHEIT);

        assertEquals(0.0f, x_data[0], 0f);
        assertEquals(4.96f, x_data[table_data.length - 1], 0.0f);

        assertEquals(356.0f, table_data[0], 0f);
        assertEquals(-40.0f, table_data[table_data.length - 1], 0.0f);
    }

    @Test
    public void test_ManifoldAirTemperatureSensorCalibration() throws IOException {
        Table iatSensorCal = newRom().findTableByName("Manifold Air Temperature Sensor Calibration");

        float[] x_data = iatSensorCal.getSeries(X).getAll();
        float[] table_data = iatSensorCal.getData().getAll();

        iatSensorCal.getData().getUnit().convert(table_data, Unit.FAHRENHEIT);

        assertEquals(0.0f, x_data[0], 0f);
        assertEquals(4.96f, x_data[table_data.length - 1], 0.0f);

        assertEquals(266.0f, table_data[0], 0f);
        assertEquals(-40.0f, table_data[table_data.length - 1], 0.0f);
    }

    @Test
    public void test_coolantTemp16BitScale() {
        float degreesCelsius = coolantTemp16BitScale.build().forward(0x59D3);
        assertEquals(degreesCelsius, 16f, 1f);
    }

    @Test
    @Disabled
    public void test_WriteCsvs() throws IOException {
        Rom rom = newRom();
        for (Table table : rom.getTables()) {
            table.writeCsv(new FileOutputStream("tables/" + testRomName + "/" + table.getName() + ".csv"));
        }
    }

}
