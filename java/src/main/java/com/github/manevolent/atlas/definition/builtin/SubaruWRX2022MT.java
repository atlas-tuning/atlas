package com.github.manevolent.atlas.definition.builtin;

import com.github.manevolent.atlas.definition.*;
import com.github.manevolent.atlas.definition.source.MemoryFlashSource;
import com.github.manevolent.atlas.definition.subaru.SubaruDITFlashEncryption;

import java.io.IOException;
import java.nio.ByteOrder;

import static com.github.manevolent.atlas.definition.Axis.X;
import static com.github.manevolent.atlas.definition.Axis.Y;
import static com.github.manevolent.atlas.definition.Unit.*;
import static com.github.manevolent.atlas.definition.Unit.PSI;

public class SubaruWRX2022MT {

    public static final String testRomName = "BE1DA813";
    public static final String fileFormat = "%s.bin";
    public static final String romResource = "/roms/" + String.format(fileFormat, testRomName);

    public static final Scale.Builder rpm_16bit = Scale.builder()
            .withName("RPM")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)
            .withUnit(RPM);

    public static final Scale.Builder rpm_8bit = Scale.builder()
            .withName("RPM")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.LSHIFT, 8)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)
            .withUnit(RPM);

    public static final Scale.Builder rpm_8bit_2 = Scale.builder()
            .withName("RPM (2)")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.LSHIFT, 3)
            .withOperation(ArithmeticOperation.MULTIPLY, 16f)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)
            .withUnit(RPM);

    public static final Scale.Builder req_torque_16bit = Scale.builder()
            .withName("Requested Torque")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.SUBTRACT, 0x3E80)
            .withOperation(ArithmeticOperation.DIVIDE, 0x50)
            .withUnit(NM);

    public static final Scale.Builder calculated_load_16bit = Scale.builder()
            .withName("Calculated Load")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.00006103515625f)
            .withUnit(G_PER_REV);

    public static final Scale.Builder calculated_load_8bit = Scale.builder()
            .withName("Calculated Load")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.LSHIFT, 8)
            .withOperations(calculated_load_16bit);

    public static final Scale.Builder percent_8bit = Scale.builder()
            .withName("Percent")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.DIVIDE, 255.0f)
            .withOperation(ArithmeticOperation.MULTIPLY, 100)
            .withUnit(PERCENT);

    public static final Scale.Builder directInjectionFuelPressureScale_16bit = Scale.builder()
            .withName("DI Fuel Pressure")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, (float)0x7D)
            .withOperation(ArithmeticOperation.RSHIFT, 0xB)
            .withOperation(ArithmeticOperation.MULTIPLY, 10.0f)
            .withUnit(KPA);

    public static final Scale.Builder directInjectionFuelPressureScale_8bit = Scale.builder()
            .withName("DI Fuel Pressure")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.MULTIPLY, (float)0x96)
            .withOperations(directInjectionFuelPressureScale_16bit);

    public static final Scale.Builder boostTargetPressureScale_16bit = Scale.builder()
            .withName("Boost Target Abs.")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.ADD, 0x6A6)
            .withOperation(ArithmeticOperation.MULTIPLY, 2)
            .withOperation(ArithmeticOperation.RSHIFT, 8)
            .withUnit(KPA);

    public static final Scale.Builder barometricPressure_16bit = Scale.builder()
            .withName("Barometric Pressure")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.RSHIFT, 8)
            .withOperation(ArithmeticOperation.DIVIDE, 6.895f)
            .withUnit(PSI);

    public static final Scale.Builder manifoldPressure_16bit = Scale.builder()
            .withName("Manifold Pressure")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 2)
            .withOperations(barometricPressure_16bit)
            .withUnit(PSI);

    public static final Scale.Builder boostErrorPressure_16bit = Scale.builder()
            .withName("Boost Error")
            .withFormat(DataFormat.SSHORT) // This is important
            .withOperations(manifoldPressure_16bit);

    public static final Scale.Builder boostTargetPressureScale_RelSL_16bit = Scale.builder()
            .withName("Boost Target rel. S/L")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.ADD, 0x6A6)
            .withOperation(ArithmeticOperation.MULTIPLY, 2)
            .withOperations(barometricPressure_16bit)
            .withOperation(ArithmeticOperation.SUBTRACT, (float) 14.70)
            .withUnit(PSI);

    public static final Scale.Builder boostTargetCompensation_8bit = Scale.builder()
            .withName("Boost Target Compensation")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.SUBTRACT, 0x55)
            .withOperation(ArithmeticOperation.MULTIPLY, 1/85f) // This takes a while to figure out
            .withOperation(ArithmeticOperation.MULTIPLY, 100f)
            .withUnit(Unit.PERCENT);

    public static final Scale.Builder coolantTemp16BitScale = Scale.builder()
            .withName("Coolant Temperature")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 5.0f)
            .withOperation(ArithmeticOperation.RSHIFT, 0xB)
            .withOperation(ArithmeticOperation.SUBTRACT, 40.0f)
            .withUnit(CELSIUS);

    public static final Scale.Builder coolantTemp8BitScale = Scale.builder()
            .withName("Coolant Temperature")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.LSHIFT, 8)
            .withOperations(coolantTemp16BitScale);


    public static final Scale.Builder wastegatePosition16bitScale = Scale.builder()
            .withName("Wastegate Position")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.SUBTRACT, 0x4000)
            .withOperation(ArithmeticOperation.DIVIDE, 0x666)
            .withUnit(Unit.MILLIMETER);

    public static final Scale.Builder wastegatePositionErrorCorr16bitScale = Scale.builder()
            .withName("Wastegate Position Error Corr.")
            .withFormat(DataFormat.SSHORT)
            .withOperation(ArithmeticOperation.DIVIDE, 0x666)
            .withUnit(Unit.MILLIMETER);

    public static final Scale.Builder intakeAirTemperature8bitScale = Scale.builder()
            .withName("Intake Air Temperature")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.SUBTRACT, 50)
            .withUnit(Unit.CELSIUS);

    public static final Scale.Builder baseIgnitionTiming = Scale.builder()
            .withName("Base Ignition Timing")
            .withOperations(
                    ScalingOperation.from(ArithmeticOperation.SUBTRACT, 0x20),
                    ScalingOperation.from(ArithmeticOperation.DIVIDE, 2)
            )
            .withFormat(DataFormat.UBYTE)
            .withUnit(Unit.DEGREES);

    private static final Scale.Builder sensorVoltageScale = Scale.builder()
            .withName("Sensor Voltage")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.DIVIDE, 13106.25f)
            .withUnit(Unit.VOLTS);

    private static final Scale.Builder sensorTemperatureScale = Scale.builder()
            .withName("Sensor Temperature")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.SUBTRACT, 50)
            .withUnit(CELSIUS);

    public static final Scale.Builder ignitionTimingCompensationScale = Scale.builder()
            .withName("Ignition Timing Compensation")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.DIVIDE, 2)
            .withUnit(Unit.DEGREES);

    public static final Scale.Builder ignitionTimingDynamicAdvanceScale = Scale.builder()
            .withName("Dynamic Advance Compensation")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.DIVIDE, 2)
            .withUnit(Unit.DEGREES);

    private static final Scale.Builder ignitionTimingIatCompensationScale = Scale.builder()
            .withName("Ignition Timing IAT Compensation")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 5f)
            .withOperation(ArithmeticOperation.ADD, 20480f)
            .withOperation(ArithmeticOperation.DIVIDE, 2048f)
            .withUnit(Unit.CELSIUS);

    public static final Scale.Builder ignitionTimingCoolantCompensationScale = Scale.builder()
            .withName("Ignition Timing Coolant Compensation")
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.DIVIDE, 10)
            .withUnit(DEGREES);

    private static Scale.Builder percent_8bit_negative = Scale.builder()
            .withName("Percent (-50 to 50%)")
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.SUBTRACT, 0x80)
            .withOperation(ArithmeticOperation.DIVIDE, 0xFF)
            .withOperation(ArithmeticOperation.MULTIPLY, 100)
            .withUnit(PERCENT);

    public static Table.Builder ignitionTimingBaseTable(MemorySection code, String name, int timingDataAddress,
                                                        int rpmAddress, int loadAddress) {
        return Table.builder()
                .withName("Ignition Timing - Base - " + name)
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, timingDataAddress)
                        .withScale(baseIgnitionTiming))
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x16)
                        .withScale(rpm_16bit))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(0x1E)
                        .withScale(calculated_load_16bit));
    }

    public static Table.Builder dynamicAdvanceIgnitionTimingTable(MemorySection code, String name, int timingDataAddress,
                                                                  int rpmAddress, int rpmLength, int loadAddress, int loadLength) {
        return Table.builder()
                .withName("Ignition Timing - Dynamic Advance - " + name)
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, timingDataAddress)
                        .withScale(ignitionTimingDynamicAdvanceScale))
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(rpmLength)
                        .withScale(rpm_16bit))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(loadLength)
                        .withScale(calculated_load_16bit));
    }

    public static Table.Builder fuelPressureTargetWarmupTable_1D(MemorySection code,
                                                                 String name,
                                                                 int dataAddress,
                                                                 int coolantTempAddress) {
        return Table.builder()
                .withName("Fuel Pressure Target - " + name)
                .withData(Series.builder()
                        .withName("Fuel Pressure")
                        .withAddress(code, dataAddress)
                        .withScale(directInjectionFuelPressureScale_16bit)
                )
                .withAxis(X, Series.builder()
                        .withName("Coolant Temperature")
                        .withAddress(code, coolantTempAddress)
                        .withLength(0x10)
                        .withScale(coolantTemp16BitScale)
                );
    }


    public static Table.Builder fuelPressureTargetWarmupTable_2D(MemorySection code,
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
                        .withScale(_16bit ? directInjectionFuelPressureScale_16bit :
                                directInjectionFuelPressureScale_8bit)
                )
                .withAxis(Y, Series.builder()
                        .withName("Coolant Temperature")
                        .withAddress(code, coolantTempAddress)
                        .withLength(0x8)
                        .withScale(coolantTemp16BitScale)
                )
                .withAxis(X, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x4)
                        .withScale(rpm_8bit)
                );
    }


    public static Table.Builder fuelPressureTargetMainTable_2D(MemorySection code,
                                                               String name,
                                                               int dataAddress,
                                                               int rpmAddress,
                                                               int loadAddress) {
        return Table.builder()
                .withName("Fuel Pressure Target - " + name)
                .withData(Series.builder()
                        .withName("Fuel Pressure")
                        .withAddress(code, dataAddress)
                        .withScale(directInjectionFuelPressureScale_8bit)
                )
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x1E)
                        .withScale(rpm_8bit))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(0x10)
                        .withScale(calculated_load_16bit));
    }

    public static Table.Builder ignitionTimingGearCompTable(MemorySection code,
                                                            String gear,
                                                            int dataAddress,
                                                            int rpmAddress,
                                                            int loadAddress) {

        return Table.builder()
                .withName("Ignition Timing - Compensation - Gear - " + gear)
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, dataAddress)
                        .withScale(ignitionTimingCompensationScale)
                )
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x03)
                        .withScale(rpm_16bit))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(0x03)
                        .withScale(calculated_load_16bit));
    }

    public static Table.Builder[] ignitionTimingIatCompTables(MemorySection code,
                                                              String name,
                                                              int dataAddress,
                                                              int iatAddress,
                                                              int activationDataAddress,
                                                              int activationRpmAddress,
                                                              int activationCalcLoadAddress) {
        return new Table.Builder[] {
                Table.builder()
                        .withName("Ignition Timing - Compensation - IAT - " + name)
                        .withData(Series.builder()
                                .withName("Timing")
                                .withAddress(code, dataAddress)
                                .withScale(ignitionTimingCompensationScale)
                        )
                        .withAxis(X, Series.builder()
                        .withName("Air Temperature")
                        .withAddress(code, iatAddress)
                        .withLength(0x10)
                        .withScale(ignitionTimingIatCompensationScale)
                ),
                Table.builder()
                        .withName("Ignition Timing - Compensation - IAT - " + name + " Activation")
                        .withData(Series.builder()
                                .withName("Activation")
                                .withAddress(code, activationDataAddress)
                                .withScale(percent_8bit)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, activationRpmAddress)
                                .withLength(0x16)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, activationCalcLoadAddress)
                        .withLength(0x1E)
                        .withScale(calculated_load_16bit)
                )
        };
    }

    public static Table.Builder ignitionTimingCoolantCompTable(MemorySection code,
                                                               String name,
                                                               int dataAddress,
                                                               DataFormat dataFormat,
                                                               Scale.Builder dataScale,
                                                               int coolantAddress) {
        return Table.builder()
                .withName("Ignition Timing - Compensation - Coolant - " + name)
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, dataAddress)
                        .withScale(dataScale)
                )
                .withAxis(X, Series.builder()
                        .withName("Coolant Temperature")
                        .withAddress(code, coolantAddress)
                        .withLength(0x10)
                        .withScale(coolantTemp16BitScale)
                );
    }

    public static Table.Builder ignitionTimingCoolantActivationTable(MemorySection code,
                                                                     String name,
                                                                     int activationDataAddress,
                                                                     DataFormat activationFormat,
                                                                     Scale.Builder activationScale,
                                                                     int activationRpmAddress,
                                                                     int rpmLength,
                                                                     DataFormat rpmFormat,
                                                                     Scale.Builder rpmScale,
                                                                     int activationCalcLoadAddress,
                                                                     int calcLoadLength,
                                                                     DataFormat calcLoadFormat,
                                                                     Scale.Builder calcLoadScale) {
        return Table.builder()
                .withName("Ignition Timing - Compensation - Coolant - " + name + " Activation")
                .withData(Series.builder()
                        .withName("Activation")
                        .withAddress(code, activationDataAddress)
                        .withScale(activationScale)
                )
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, activationRpmAddress)
                        .withLength(rpmLength)
                        .withScale(rpmScale))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, activationCalcLoadAddress)
                        .withLength(calcLoadLength)
                        .withScale(calcLoadScale)
                );
    }

    public static Table.Builder[] ignitionTimingCoolantCompTables(MemorySection code,
                                                                  String name,
                                                                  int dataAddress,
                                                                  int coolantAddress,
                                                                  int activationDataAddress,
                                                                  int activationRpmAddress,
                                                                  int activationCalcLoadAddress) {
        return new Table.Builder[] {
                ignitionTimingCoolantCompTable(code, name,
                        dataAddress, DataFormat.USHORT, ignitionTimingCoolantCompensationScale,
                        coolantAddress),
                ignitionTimingCoolantActivationTable(code, name,
                        activationDataAddress,
                        DataFormat.USHORT,
                        percent_8bit, //TODO this was /100, not /255
                        activationRpmAddress, 0x16, DataFormat.UBYTE, rpm_8bit_2,
                        activationCalcLoadAddress, 0x1E, DataFormat.UBYTE, calculated_load_8bit
                )
        };
    }

    public static final Table.Builder singleValueTable(MemorySection code, String name, int dataAddress,
                                                       Scale.Builder scale) {
        return Table.builder()
                .withName(name)
                .withData(Series.builder()
                        .withAddress(code, dataAddress)
                        .withLength(1)
                        .withScale(scale)
                );
    }

    public static Rom newRom() throws IOException {
        MemorySection code = new MemorySection();
        code.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        code.setBaseAddress(0x0);
        code.setEncryption(SubaruDITFlashEncryption.WRX_MT_2022_USDM);
        code.setSource(MemoryFlashSource.from(SubaruWRX2022MT.class.getResourceAsStream(romResource)));
        code.setDataLength(0x003F0000);

        return Rom.builder()
                .withVehicle(Vehicle.builder()
                        .withYear("2022")
                        .withMarket("USDM")
                        .withMake("Subaru")
                        .withModel("WRX")
                        .withTrim("Premium")
                        .withTransmission("MT"))
                .withSection(code)
                .withScales(
                        rpm_16bit,
                        rpm_8bit,
                        rpm_8bit_2,
                        req_torque_16bit,
                        calculated_load_16bit,
                        calculated_load_8bit,
                        percent_8bit,
                        percent_8bit_negative,
                        directInjectionFuelPressureScale_16bit,
                        directInjectionFuelPressureScale_8bit,
                        boostTargetPressureScale_16bit,
                        barometricPressure_16bit,
                        manifoldPressure_16bit,
                        boostErrorPressure_16bit,
                        boostTargetPressureScale_RelSL_16bit,
                        boostTargetCompensation_8bit,
                        coolantTemp16BitScale,
                        coolantTemp8BitScale,
                        wastegatePosition16bitScale,
                        wastegatePositionErrorCorr16bitScale,
                        intakeAirTemperature8bitScale,
                        baseIgnitionTiming,
                        sensorTemperatureScale,
                        sensorVoltageScale,
                        ignitionTimingCompensationScale,
                        ignitionTimingIatCompensationScale,
                        ignitionTimingDynamicAdvanceScale,
                        ignitionTimingCoolantCompensationScale
                )
                .withTable(
                        Table.builder()
                                .withName("Sensor Calibration - Intake Air Temperature")
                                .withData(Series.builder()
                                        .withAddress(code, 0x00029780)
                                        .withScale(sensorTemperatureScale))
                                .withAxis(X, Series.builder()
                                        .withName("Sensor Voltage")
                                        .withAddress(code, 0x000297c0)
                                        .withScale(sensorVoltageScale)
                                        .withLength(32))
                ).withTable(
                        Table.builder()
                                .withName("Sensor Calibration - Manifold Air Temperature")
                                .withData(Series.builder()
                                        .withAddress(code, 0x000297a0)
                                        .withScale(sensorTemperatureScale))
                                .withAxis(X, Series.builder()
                                        .withName("Sensor Voltage")
                                        .withAddress(code, 0x000297c0)
                                        .withScale(sensorVoltageScale)
                                        .withLength(32))
                )
                // Ignition timing base tables
                .withTable(ignitionTimingBaseTable(code, "TGVs Closed - AVCS Disabled", 0x000ad514, 0x000a88b4, 0x000a89bc))
                .withTable(ignitionTimingBaseTable(code, "TGVs Closed - AVCS Enabled", 0x000ad7a8, 0x000a88b4, 0x000a89bc))
                .withTable(ignitionTimingBaseTable(code, "TGVs Open - AVCS Disabled", 0x000ada3c, 0x000a88b4, 0x000a89bc))
                .withTable(ignitionTimingBaseTable(code, "TGVs Open - AVCS Enabled", 0x000adcd0, 0x000a88b4, 0x000a89bc))

                // Ignition timing compensation by gear
                .withTable(ignitionTimingGearCompTable(code, "1st", 0x000a8bf0, 0x000a7478, 0x000a7470))
                .withTable(ignitionTimingGearCompTable(code, "2nd", 0x000a8bfc, 0x000a7478, 0x000a7470))
                .withTable(ignitionTimingGearCompTable(code, "3rd", 0x000a8c08, 0x000a7478, 0x000a7470))
                .withTable(ignitionTimingGearCompTable(code, "4th", 0x000a8c14, 0x000a7478, 0x000a7470))
                .withTable(ignitionTimingGearCompTable(code, "5th", 0x000a8c20, 0x000a7478, 0x000a7470))

                // Ignition timing compensation by IAT
                .withTables(ignitionTimingIatCompTables(code, "A", 0x000a80ec, 0x000a80cc,
                        0x000ac830, 0x000a8914, 0x000a8980))
                .withTables(ignitionTimingIatCompTables(code, "B", 0x000a7b5c, 0x000a80cc,
                        0x000acac4, 0x000a8914, 0x000a8980))

                // Dynamic advance timing
                .withTable(dynamicAdvanceIgnitionTimingTable(code, "TGVs Closed (Base)", 0x000b5fc0, 0x000b54e4, 0x16, 0x000b5510, 0x1E))
                .withTable(dynamicAdvanceIgnitionTimingTable(code, "TGVs Closed (Adder)", 0x000b64e8, 0x000b5624, 0x15, 0x000b5578, 0x1E))
                .withTable(dynamicAdvanceIgnitionTimingTable(code, "TGVs Open (Base) ", 0x000b6254, 0x000b54e4, 0x16, 0x000b5510, 0x1E))
                .withTable(dynamicAdvanceIgnitionTimingTable(code, "TGVs Open (Adder)", 0x000b6760, 0x000b5650, 0x15, 0x000b55b4, 0x1E))

                //Ignition timing compensation by coolant temperature
                .withTables(ignitionTimingCoolantCompTables(code, "TGVs Closed", 0x000a7e9c, 0x00030a68, 0x000abde0, 0x000a8b34, 0x000a8b6c))
                .withTables(ignitionTimingCoolantCompTables(code, "TGVs Open", 0x000a7ebc, 0x00030a68, 0x000ac308, 0x000a8b34, 0x000a8b6c))
                .withTable(ignitionTimingCoolantActivationTable(code, "Cold Start (TGVs Closed)",
                        0x000a9490,
                        DataFormat.UBYTE,
                        percent_8bit_negative,
                        0x000a8888, 0x16, DataFormat.USHORT, rpm_16bit,
                        0x000a797c, 0x10, DataFormat.USHORT, calculated_load_16bit))
                .withTable(ignitionTimingCoolantActivationTable(code, "Cold Start (TGVs Open)",
                        0x000a95f0,
                        DataFormat.UBYTE,
                        percent_8bit_negative,
                        0x000a8888, 0x16, DataFormat.USHORT, rpm_16bit,
                        0x000a797c, 0x10, DataFormat.USHORT, calculated_load_16bit))
                .withTable(ignitionTimingCoolantCompTable(code, "Cold Start",
                        0x000a7b2c,
                        DataFormat.UBYTE,
                        percent_8bit_negative,
                        0x000a7b0c))

                // Fuel pressure
                .withTable(Table.builder()
                        .withName("Fuel Pressure Target - Main - Adder Activation")
                        .withData(Series.builder()
                                .withName("Activation")
                                .withAddress(code, 0x000c84a4)
                                .withScale(percent_8bit)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("Intake Air Temperature")
                                .withAddress(code, 0x000c82f0)
                                .withLength(0x8)
                                .withScale(sensorTemperatureScale))
                        .withAxis(X, Series.builder()
                                .withName("Coolant Temperature")
                                .withAddress(code, 0x000c82e8)
                                .withLength(0x8)
                                .withScale(coolantTemp8BitScale)))
                .withTable(fuelPressureTargetMainTable_2D(code, "Main - Adder", 0x000c9320, 0x000c8880, 0x000c8494))
                .withTable(fuelPressureTargetMainTable_2D(code, "Main - TGVs Closed", 0x000cb2f0, 0x000c8880, 0x000c83a4))
                .withTable(fuelPressureTargetMainTable_2D(code, "Main - TGVs Open", 0x000cb4d0, 0x000c8880, 0x000c83a4))
                .withTable(fuelPressureTargetMainTable_2D(code, "Main - Idle", 0x000c9140, 0x000c8880, 0x000c83a4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup - Mode 3A #1", 0x000c85c4, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup - Mode 3A #2", 0x000c85a4, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup - Mode 1A", 0x000c85e4, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup - Mode 1A", 0x000c85e4, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup - Mode 1B #1", 0x000c8564, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup - Mode 1B #2", 0x000c8504, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup - Mode 3B #1", 0x000c8544, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup - Mode 3B #2", 0x000c8524, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_2D(code, "Warmup - Mode 4", 0x000c8654, true, 0x000c83e4, 0x000c82f8))
                .withTable(fuelPressureTargetWarmupTable_2D(code, "Warmup - Default", 0x000c83c4, false, 0x000c83b4, 0x000c82f8))
                .withTable(Table.builder()
                        .withName("Boost Target - Main")
                        .withData(Series.builder()
                                .withName("Boost (rel. sea level)")
                                .withAddress(code, 0x0002cda8)
                                .withScale(boostTargetPressureScale_RelSL_16bit)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002afd8)
                                .withLength(0x13)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Requested Torque")
                                .withAddress(code, 0x0002d300)
                                .withLength(0x24)
                                .withScale(req_torque_16bit)))
                .withTable(Table.builder()
                        .withName("Boost Target - Compensation - IAT")
                        .withData(Series.builder()
                                .withName("Percent")
                                .withAddress(code, 0x0002a318)
                                .withScale(boostTargetCompensation_8bit)
                        )
                        .withAxis(X, Series.builder()
                                .withName("Intake Air Temperature")
                                .withAddress(code, 0x00036e6c)
                                .withLength(0x10)
                                .withScale(intakeAirTemperature8bitScale)
                        )

                )
                .withTable(Table.builder()
                        .withName("Boost Target - Compensation - IAT")
                        .withData(Series.builder()
                                .withName("Percent")
                                .withAddress(code, 0x0002c594)
                                .withScale(boostTargetCompensation_8bit)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002afa8)
                                .withLength(0xB)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Barometric Pressure")
                                .withAddress(code, 0x0002afc0)
                                .withLength(0xB)
                                .withScale(barometricPressure_16bit)
                        )
                )
                .withTable(singleValueTable(code, "Boost Target - Limits - Maximum Limit",
                        0x00028bf0,
                        boostTargetPressureScale_RelSL_16bit
                ))
                .withTable(singleValueTable(code, "Boost Target - Compensation - IAT DTC",
                        0x00028fe6,
                        boostTargetCompensation_8bit
                ))
                .withTable(singleValueTable(code, "Boost Target - Compensation - Barometric DTC",
                        0x00028fe7,
                        boostTargetCompensation_8bit
                ))
                .withTable(Table.builder()
                        .withName("Wastegate Position - Main")
                        .withData(Series.builder()
                                .withName("Position")
                                .withAddress(code, 0x0002c970)
                                .withScale(wastegatePosition16bitScale)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002ae38)
                                .withLength(0x12)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Target Boost Pressure")
                                .withAddress(code, 0x0002ae5c)
                                .withLength(0x1E)
                                .withScale(boostTargetPressureScale_RelSL_16bit)
                        )
                ).withTable(Table.builder()
                        .withName("Wastegate Position - Compensation - IAT")
                        .withData(Series.builder()
                                .withName("Percent")
                                .withAddress(code, 0x0002c610)
                                .withScale(boostTargetCompensation_8bit)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002ae98)
                                .withLength(0x12)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Intake Air Temperature")
                                .withAddress(code, 0x0002a328)
                                .withLength(0x10)
                                .withScale(intakeAirTemperature8bitScale)
                        )
                ).withTable(Table.builder()
                        .withName("Wastegate Position - Compensation - Barometric")
                        .withData(Series.builder()
                                .withName("Percent")
                                .withAddress(code, 0x0002c730)
                                .withScale(boostTargetCompensation_8bit)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002ae98)
                                .withLength(0x12)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Barometric Temperature")
                                .withAddress(code, 0x0002a3b8)
                                .withLength(0x10)
                                .withScale(barometricPressure_16bit)
                        )
                ).withTable(singleValueTable(code, "Wastegate Position - Compensation - Transient Boost Threshold",
                        0x00028c1e,
                        boostErrorPressure_16bit
                )).withTable(Table.builder()
                        .withName("Wastegate Position - Boost Error Compensation - Torque Exceeded")
                        .withData(Series.builder()
                                .withName("Position")
                                .withAddress(code, 0x0002d348)
                                .withScale(wastegatePositionErrorCorr16bitScale)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002aebc)
                                .withLength(0x12)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Boost Error")
                                .withAddress(code, 0x0002af28)
                                .withLength(0x1E)
                                .withScale(boostErrorPressure_16bit)
                        )
                ).withTable(Table.builder()
                        .withName("Wastegate Position - Boost Error Compensation - Transient Boost")
                        .withData(Series.builder()
                                .withName("Position")
                                .withAddress(code, 0x0002d780)
                                .withScale(wastegatePositionErrorCorr16bitScale)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002aebc)
                                .withLength(0x12)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Boost Error")
                                .withAddress(code, 0x0002af28)
                                .withLength(0x1E)
                                .withScale(boostErrorPressure_16bit)
                        )
                )
                .withTable(singleValueTable(code, "Wastegate Position - Boost Error Compensation - Maximum Limit",
                        0x00028c1a, wastegatePosition16bitScale))
                .withTable(singleValueTable(code, "Wastegate Position - Boost Error Compensation - Minimum Limit",
                        0x00028c18, wastegatePosition16bitScale))
                .withTable(singleValueTable(code, "Wastegate Position - Maximum Limit",
                        0x00028c00, wastegatePosition16bitScale))
                .withTable(singleValueTable(code, "Wastegate Position - Minimum Limit",
                        0x00028bfe, wastegatePosition16bitScale))
                .withTable(Table.builder()
                        .withName("Turbo Limits - Minimum RPM")
                        .withData(Series.builder()
                                .withName("Minimum RPM")
                                .withAddress(code, 0x0002a378)
                                .withScale(rpm_16bit)
                        )
                        .withAxis(X, Series.builder()
                                .withName("Coolant Temperature")
                                .withAddress(code, 0x0002a398)
                                .withLength(0x10)
                                .withScale(coolantTemp16BitScale)))
                .withTable(Table.builder()
                        .withName("Turbo Limits - Maximum Torque - Main")
                        .withData(Series.builder()
                                .withName("Requested Torque")
                                .withAddress(code, 0x0002aee0)
                                .withScale(req_torque_16bit)
                        )
                        .withAxis(X, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002af04)
                                .withLength(0x12)
                                .withScale(rpm_16bit)))
                .withTable(Table.builder()
                        .withName("Turbo Limits - Maximum Torque - Barometric Compensation")
                        .withData(Series.builder()
                                .withName("Percent")
                                .withAddress(code, 0x0002e3e0)
                                .withScale(boostTargetCompensation_8bit)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002b020)
                                .withLength(0x0B)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Barometric Temperature")
                                .withAddress(code, 0x0002b038)
                                .withLength(0x0B)
                                .withScale(barometricPressure_16bit)
                        )
                ).withTable(Table.builder()
                        .withName("Turbo Limits - Maximum Torque - IAT Compensation")
                        .withData(Series.builder()
                                .withName("Percent")
                                .withAddress(code, 0x0002ec5c)
                                .withScale(boostTargetCompensation_8bit)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002a3f8)
                                .withLength(0x10)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Intake Air Temperature")
                                .withAddress(code, 0x0002a658)
                                .withLength(0x10)
                                .withScale(intakeAirTemperature8bitScale)
                        )
                )
                .build();
    }
}
