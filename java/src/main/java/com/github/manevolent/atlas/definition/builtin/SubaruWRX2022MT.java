package com.github.manevolent.atlas.definition.builtin;

import com.github.manevolent.atlas.definition.*;
import com.github.manevolent.atlas.definition.source.MemoryFlashSource;
import com.github.manevolent.atlas.definition.subaru.SubaruDITFlashEncryption;
import com.github.manevolent.atlas.definition.source.StreamedFlashSource;

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
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)
            .withUnit(RPM);

    public static final Scale.Builder rpm_8bit = Scale.builder()
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.LSHIFT, 8)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)
            .withUnit(RPM);

    public static final Scale.Builder rpm_8bit_2 = Scale.builder()
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.LSHIFT, 3)
            .withOperation(ArithmeticOperation.MULTIPLY, 16f)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)
            .withUnit(RPM);

    public static final Scale.Builder req_torque_16bit = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.SUBTRACT, 0x3E80)
            .withOperation(ArithmeticOperation.DIVIDE, 0x50)
            .withUnit(NM);

    public static final Scale.Builder calculated_load_16bit = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 0.00006103515625f)
            .withUnit(G_PER_REV);

    public static final Scale.Builder calculated_load_8bit = Scale.builder()
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.LSHIFT, 8)
            .withOperations(calculated_load_16bit);

    public static final Scale.Builder percent_8bit = Scale.builder()
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.DIVIDE, 255.0f)
            .withUnit(PERCENT);

    public static final Scale.Builder directInjectionFuelPressureScale_16bit = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, (float)0x7D)
            .withOperation(ArithmeticOperation.RSHIFT, 0xB)
            .withOperation(ArithmeticOperation.MULTIPLY, 10.0f)
            .withUnit(KPA);

    public static final Scale.Builder directInjectionFuelPressureScale_8bit = Scale.builder()
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.MULTIPLY, (float)0x96)
            .withOperations(directInjectionFuelPressureScale_16bit);
    public static final Scale.Builder boostTargetPressureScale_16bit = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.ADD, 0x6A6)
            .withOperation(ArithmeticOperation.MULTIPLY, 2)
            .withOperation(ArithmeticOperation.RSHIFT, 8)
            .withUnit(KPA);

    public static final Scale.Builder barometricPressure_16bit = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.RSHIFT, 8)
            .withOperation(ArithmeticOperation.DIVIDE, 6.895f)
            .withUnit(PSI);
    public static final Scale.Builder manifoldPressure_16bit = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 2)
            .withOperations(barometricPressure_16bit)
            .withUnit(PSI);
    public static final Scale.Builder boostErrorPressure_16bit = Scale.builder()
            .withFormat(DataFormat.SSHORT) // This is important
            .withOperations(manifoldPressure_16bit);
    public static final Scale.Builder boostTargetPressureScale_RelSL_16bit = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.ADD, 0x6A6)
            .withOperation(ArithmeticOperation.MULTIPLY, 2)
            .withOperations(barometricPressure_16bit)
            .withOperation(ArithmeticOperation.SUBTRACT, (float) 14.70)
            .withUnit(PSI);

    public static final Scale.Builder boostTargetCompensation_8bit = Scale.builder()
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.SUBTRACT, 0x55)
            .withOperation(ArithmeticOperation.MULTIPLY, 1/85f) // This takes a while to figure out
            .withOperation(ArithmeticOperation.MULTIPLY, 100f)
            .withUnit(Unit.PERCENT);

    public static final Scale.Builder coolantTemp16BitScale = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.MULTIPLY, 5.0f)
            .withOperation(ArithmeticOperation.RSHIFT, 0xB)
            .withOperation(ArithmeticOperation.SUBTRACT, 40.0f)
            .withUnit(CELSIUS);

    public static final Scale.Builder coolantTemp8BitScale = Scale.builder()
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.LSHIFT, 8)
            .withOperations(coolantTemp16BitScale);


    public static final Scale.Builder wastegatePosition16bitScale = Scale.builder()
            .withFormat(DataFormat.USHORT)
            .withOperation(ArithmeticOperation.SUBTRACT, 0x4000)
            .withOperation(ArithmeticOperation.DIVIDE, 0x666)
            .withUnit(Unit.MILLIMETER);
    public static final Scale.Builder wastegatePositionErrorCorr16bitScale = Scale.builder()
            .withFormat(DataFormat.SSHORT)
            .withOperation(ArithmeticOperation.DIVIDE, 0x666)
            .withUnit(Unit.MILLIMETER);

    public static final Scale.Builder intakeAirTemperature8bitScale = Scale.builder()
            .withFormat(DataFormat.UBYTE)
            .withOperation(ArithmeticOperation.SUBTRACT, 50)
            .withUnit(Unit.CELSIUS);

    public static final Scale.Builder baseIgnitionTiming = Scale.builder()
            .withOperations(
                    ScalingOperation.from(ArithmeticOperation.SUBTRACT, 0x20),
                    ScalingOperation.from(ArithmeticOperation.DIVIDE, 2)
            )
            .withFormat(DataFormat.UBYTE)
            .withUnit(Unit.DEGREES);

    public static Table.Builder ignitionTimingBaseTable(FlashRegion code, String name, int timingDataAddress,
                                                         int rpmAddress, int loadAddress) {
        return Table.builder()
                .withName("Base Ignition Timing - " + name)
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, timingDataAddress)
                        .withScale(baseIgnitionTiming))
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x16)
                        .withFormat(DataFormat.USHORT)
                        .withUnit(Unit.RPM)
                        .withScale(Scale.builder().withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(0x1E)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(DataFormat.USHORT)
                        .withScale(calculated_load_16bit));
    }

    public static Table.Builder dynamicAdvanceIgnitionTimingTable(FlashRegion code, String name, int timingDataAddress,
                                                                   int rpmAddress, int rpmLength, int loadAddress, int loadLength) {
        return Table.builder()
                .withName("Dynamic Advance Timing - " + name)
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, timingDataAddress)
                        .withFormat(DataFormat.UBYTE)
                        .withUnit(Unit.DEGREES)
                        .withScale(Scale.builder().withOperations(
                                ScalingOperation.from(ArithmeticOperation.DIVIDE, 2)
                        )))
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(rpmLength)
                        .withFormat(DataFormat.USHORT)
                        .withUnit(Unit.RPM)
                        .withScale(Scale.builder().withOperation(ArithmeticOperation.MULTIPLY, 0.1953125f)))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(loadLength)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(DataFormat.USHORT)
                        .withScale(calculated_load_16bit));
    }

    public static Table.Builder fuelPressureTargetWarmupTable_1D(FlashRegion code,
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


    public static Table.Builder fuelPressureTargetWarmupTable_2D(FlashRegion code,
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


    public static Table.Builder fuelPressureTargetMainTable_2D(FlashRegion code,
                                                                String name,
                                                                int dataAddress,
                                                                int rpmAddress,
                                                                int loadAddress) {
        return Table.builder()
                .withName("Fuel Pressure Target - " + name)
                .withData(Series.builder()
                        .withName("Fuel Pressure")
                        .withAddress(code, dataAddress)
                        .withFormat(DataFormat.UBYTE)
                        .withScale(directInjectionFuelPressureScale_8bit)
                        .withUnit(Unit.KPA)
                )
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x1E)
                        .withFormat(DataFormat.UBYTE)
                        .withUnit(Unit.RPM)
                        .withScale(rpm_8bit))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(0x10)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(DataFormat.USHORT)
                        .withScale(calculated_load_16bit));
    }

    public static Table.Builder ignitionTimingGearCompTable(FlashRegion code,
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
                        .withScale(Scale.builder().withOperations(
                                ScalingOperation.from(ArithmeticOperation.DIVIDE, 2)
                        ))
                )
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, rpmAddress)
                        .withLength(0x03)
                        .withFormat(DataFormat.USHORT)
                        .withScale(rpm_16bit)
                        .withUnit(Unit.RPM))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, loadAddress)
                        .withLength(0x03)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(DataFormat.USHORT)
                        .withScale(calculated_load_16bit));
    }

    public static Table.Builder[] ignitionTimingIatCompTables(FlashRegion code,
                                                               String name,
                                                               int dataAddress,
                                                               int iatAddress,
                                                               int activationDataAddress,
                                                               int activationRpmAddress,
                                                               int activationCalcLoadAddress) {
        return new Table.Builder[] {
                Table.builder()
                        .withName("Ignition Timing Compensation - IAT " + name)
                        .withData(Series.builder()
                                .withName("Timing")
                                .withAddress(code, dataAddress)
                                .withFormat(DataFormat.UBYTE)
                                .withUnit(Unit.DEGREES)
                                .withScale(Scale.builder().withOperations(
                                        ScalingOperation.from(ArithmeticOperation.DIVIDE, 2)
                                ))
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
                        .withName("Ignition Timing Compensation - IAT " + name + " Activation")
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
                                .withScale(rpm_16bit)
                                .withUnit(Unit.RPM))
                        .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, activationCalcLoadAddress)
                        .withLength(0x1E)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(DataFormat.USHORT)
                        .withScale(calculated_load_16bit)
                )
        };
    }

    public static Table.Builder ignitionTimingCoolantCompTable(FlashRegion code,
                                                                String name,
                                                                int dataAddress,
                                                                DataFormat dataFormat,
                                                                Scale.Builder dataScale,
                                                                int coolantAddress) {
        return Table.builder()
                .withName("Ignition Timing Compensation Coolant - " + name)
                .withData(Series.builder()
                        .withName("Timing")
                        .withAddress(code, dataAddress)
                        .withFormat(dataFormat)
                        .withUnit(Unit.DEGREES)
                        .withScale(dataScale)
                )
                .withAxis(X, Series.builder()
                        .withName("Coolant Temperature")
                        .withUnit(Unit.CELSIUS)
                        .withAddress(code, coolantAddress)
                        .withLength(0x10)
                        .withFormat(DataFormat.USHORT)
                        .withScale(coolantTemp16BitScale)
                );
    }

    public static Table.Builder ignitionTimingCoolantActivationTable(FlashRegion code,
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
                .withName("Ignition Timing Compensation Coolant Activation - " + name)
                .withData(Series.builder()
                        .withName("Activation")
                        .withAddress(code, activationDataAddress)
                        .withFormat(activationFormat)
                        .withUnit(Unit.PERCENT)
                        .withScale(activationScale)
                )
                .withAxis(Y, Series.builder()
                        .withName("RPM")
                        .withAddress(code, activationRpmAddress)
                        .withLength(rpmLength)
                        .withFormat(rpmFormat)
                        .withScale(rpmScale)
                        .withUnit(Unit.RPM))
                .withAxis(X, Series.builder()
                        .withName("Load")
                        .withAddress(code, activationCalcLoadAddress)
                        .withLength(calcLoadLength)
                        .withUnit(Unit.G_PER_REV)
                        .withFormat(calcLoadFormat)
                        .withScale(calcLoadScale)
                );
    }

    public static Table.Builder[] ignitionTimingCoolantCompTables(FlashRegion code,
                                                                   String name,
                                                                   int dataAddress,
                                                                   int coolantAddress,
                                                                   int activationDataAddress,
                                                                   int activationRpmAddress,
                                                                   int activationCalcLoadAddress) {
        return new Table.Builder[] {
                ignitionTimingCoolantCompTable(code, name,
                        dataAddress, DataFormat.USHORT, Scale.builder().withOperations(
                                ScalingOperation.from(ArithmeticOperation.DIVIDE, 10)
                        ),
                        coolantAddress),
                ignitionTimingCoolantActivationTable(code, name,
                        activationDataAddress,
                        DataFormat.USHORT,
                        Scale.builder().withOperation(ArithmeticOperation.DIVIDE, 100f),
                        activationRpmAddress, 0x16, DataFormat.UBYTE, rpm_8bit_2,
                        activationCalcLoadAddress, 0x1E, DataFormat.UBYTE, calculated_load_8bit
                )
        };
    }

    public static final Table.Builder singleValueTable(FlashRegion code, String name, int dataAddress,
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
        FlashRegion code = new FlashRegion();
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
                .withRegion(code)
                .withTable(
                        Table.builder()
                                .withName("Sensor Calibration - Intake Air Temperature")
                                .withData(Series.builder()
                                        .withAddress(code, 0x00029780)
                                        .withFormat(DataFormat.UBYTE)
                                        .withScale(Scale.builder().withOperation(ArithmeticOperation.SUBTRACT, 50))
                                        .withUnit(Unit.CELSIUS))
                                .withAxis(X, Series.builder()
                                        .withName("Sensor Voltage")
                                        .withAddress(code, 0x000297c0)
                                        .withFormat(DataFormat.USHORT)
                                        .withUnit(Unit.VOLTS)
                                        .withScale(Scale.builder().withOperation(ArithmeticOperation.DIVIDE, 13106.25f))
                                        .withLength(32))
                ).withTable(
                        Table.builder()
                                .withName("Sensor Calibration - Manifold Air Temperature")
                                .withData(Series.builder()
                                        .withAddress(code, 0x000297a0)
                                        .withFormat(DataFormat.UBYTE)
                                        .withScale(Scale.builder().withOperation(ArithmeticOperation.SUBTRACT, 50))
                                        .withUnit(Unit.CELSIUS))
                                .withAxis(X, Series.builder()
                                        .withName("Sensor Voltage")
                                        .withAddress(code, 0x000297c0)
                                        .withFormat(DataFormat.USHORT)
                                        .withUnit(Unit.VOLTS)
                                        .withScale(Scale.builder().withOperation(ArithmeticOperation.DIVIDE, 13106.25f))
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
                .withTable(dynamicAdvanceIgnitionTimingTable(code, "TGVs Closed - Base", 0x000b5fc0, 0x000b54e4, 0x16, 0x000b5510, 0x1E))
                .withTable(dynamicAdvanceIgnitionTimingTable(code, "TGVs Open - Base ", 0x000b6254, 0x000b54e4, 0x16, 0x000b5510, 0x1E))
                .withTable(dynamicAdvanceIgnitionTimingTable(code, "TGVs Closed - Adder", 0x000b64e8, 0x000b5624, 0x15, 0x000b5578, 0x1E))
                .withTable(dynamicAdvanceIgnitionTimingTable(code, "TGVs Open - Adders", 0x000b6760, 0x000b5650, 0x15, 0x000b55b4, 0x1E))

                //Ignition timing compensation by coolant temperature
                .withTables(ignitionTimingCoolantCompTables(code, "TGVs Closed", 0x000a7e9c, 0x00030a68, 0x000abde0, 0x000a8b34, 0x000a8b6c))
                .withTables(ignitionTimingCoolantCompTables(code, "TGVs Open", 0x000a7ebc, 0x00030a68, 0x000ac308, 0x000a8b34, 0x000a8b6c))
                .withTable(ignitionTimingCoolantActivationTable(code, "Cold Start (TGVs Closed)",
                        0x000a9490,
                        DataFormat.UBYTE,
                        Scale.builder().withOperation(ArithmeticOperation.SUBTRACT, 0x80).withOperation(ArithmeticOperation.DIVIDE, 0xFF).withOperation(ArithmeticOperation.MULTIPLY, 100),
                        0x000a8888, 0x16, DataFormat.USHORT, rpm_16bit,
                        0x000a797c, 0x10, DataFormat.USHORT, calculated_load_16bit))
                .withTable(ignitionTimingCoolantActivationTable(code, "Cold Start (TGVs Open)",
                        0x000a95f0,
                        DataFormat.UBYTE,
                        Scale.builder().withOperation(ArithmeticOperation.SUBTRACT, 0x80).withOperation(ArithmeticOperation.DIVIDE, 0xFF).withOperation(ArithmeticOperation.MULTIPLY, 100),
                        0x000a8888, 0x16, DataFormat.USHORT, rpm_16bit,
                        0x000a797c, 0x10, DataFormat.USHORT, calculated_load_16bit))
                .withTable(ignitionTimingCoolantCompTable(code, "Cold Start",
                        0x000a7b2c,
                        DataFormat.UBYTE,
                        Scale.builder().withOperation(ArithmeticOperation.SUBTRACT, 0x80).withOperation(ArithmeticOperation.DIVIDE, 2),
                        0x000a7b0c))

                // Fuel pressure
                .withTable(Table.builder()
                        .withName("Fuel Pressure Target - Main - Adder Activation")
                        .withData(Series.builder()
                                .withName("Activation")
                                .withAddress(code, 0x000c84a4)
                                .withFormat(DataFormat.UBYTE)
                                .withScale(percent_8bit)
                                .withUnit(Unit.PERCENT)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("Intake Air Temperature")
                                .withAddress(code, 0x000c82f0)
                                .withLength(0x8)
                                .withFormat(DataFormat.UBYTE)
                                .withScale(Scale.builder().withOperation(ArithmeticOperation.SUBTRACT, 50))
                                .withUnit(Unit.CELSIUS))
                        .withAxis(X, Series.builder()
                                .withName("Coolant Temperature")
                                .withAddress(code, 0x000c82e8)
                                .withLength(0x8)
                                .withUnit(Unit.G_PER_REV)
                                .withFormat(DataFormat.UBYTE)
                                .withScale(coolantTemp8BitScale)))
                .withTable(fuelPressureTargetMainTable_2D(code, "Main - Adder", 0x000c9320, 0x000c8880, 0x000c8494))
                .withTable(fuelPressureTargetMainTable_2D(code, "Main - TGVs Closed", 0x000cb2f0, 0x000c8880, 0x000c83a4))
                .withTable(fuelPressureTargetMainTable_2D(code, "Main - TGVs Open", 0x000cb4d0, 0x000c8880, 0x000c83a4))
                .withTable(fuelPressureTargetMainTable_2D(code, "Main - Idle", 0x000c9140, 0x000c8880, 0x000c83a4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup Mode 3A #1", 0x000c85c4, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup Mode 3A #2", 0x000c85a4, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup Mode 1A", 0x000c85e4, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup Mode 1A", 0x000c85e4, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup Mode 1B #1", 0x000c8564, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup Mode 1B #2", 0x000c8504, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup Mode 3B #1", 0x000c8544, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_1D(code, "Warmup Mode 3B #2", 0x000c8524, 0x000c84e4))
                .withTable(fuelPressureTargetWarmupTable_2D(code, "Warmup Mode 4", 0x000c8654, true, 0x000c83e4, 0x000c82f8))
                .withTable(fuelPressureTargetWarmupTable_2D(code, "Warmup Default", 0x000c83c4, false, 0x000c83b4, 0x000c82f8))
                .withTable(Table.builder()
                        .withName("Boost Target - Main")
                        .withData(Series.builder()
                                .withName("Boost (rel. sea level)")
                                .withAddress(code, 0x0002cda8)
                                .withFormat(DataFormat.USHORT)
                                .withScale(boostTargetPressureScale_RelSL_16bit)
                                .withUnit(PSI)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002afd8)
                                .withLength(0x13)
                                .withFormat(DataFormat.USHORT)
                                .withUnit(Unit.RPM)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Requested Torque")
                                .withAddress(code, 0x0002d300)
                                .withLength(0x24)
                                .withUnit(Unit.NM)
                                .withFormat(DataFormat.USHORT)
                                .withScale(req_torque_16bit)))
                .withTable(Table.builder()
                        .withName("Boost Target - IAT Compensation")
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
                        .withName("Boost Target - Barometric Compensation")
                        .withData(Series.builder()
                                .withName("Percent")
                                .withAddress(code, 0x0002c594)
                                .withScale(boostTargetCompensation_8bit)
                                .withFormat(DataFormat.UBYTE)
                                .withUnit(Unit.PERCENT)
                        )
                        .withAxis(Y, Series.builder()
                                .withName("RPM")
                                .withAddress(code, 0x0002afa8)
                                .withLength(0xB)
                                .withFormat(DataFormat.USHORT)
                                .withUnit(Unit.RPM)
                                .withScale(rpm_16bit))
                        .withAxis(X, Series.builder()
                                .withName("Barometric Pressure")
                                .withAddress(code, 0x0002afc0)
                                .withLength(0xB)
                                .withScale(barometricPressure_16bit)
                                .withFormat(DataFormat.USHORT)
                                .withUnit(PSI)
                        )
                )
                .withTable(singleValueTable(code, "Boost Target - Maximum Limit",
                        0x00028bf0,
                        boostTargetPressureScale_RelSL_16bit
                ))
                .withTable(singleValueTable(code, "Boost Target - IAT Compensation - DTC",
                        0x00028fe6,
                        boostTargetCompensation_8bit
                ))
                .withTable(singleValueTable(code, "Boost Target - Barometric Compensation - DTC",
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
                        .withName("Wastegate Position - IAT Compensation")
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
                        .withName("Wastegate Position - Barometric Compensation")
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
                ).withTable(singleValueTable(code, "Wastegate Position - Boost Error Compensation - Transient Boost Threshold",
                        0x00028c1e,
                        boostErrorPressure_16bit
                )).withTable(Table.builder()
                        .withName("Wastegate Position - Boost Error Compensation - Continuous")
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
                        .withName("Turbo Limits - Coolant")
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
                        .withName("Turbo Limits - Requested Torque")
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
                        .withName("Turbo Limits - Requested Torque - Barometric Compensation")
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
                        .withName("Turbo Limits - Requested Torque - IAT Compensation")
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
