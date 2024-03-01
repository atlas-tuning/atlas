package com.github.manevolent.atlas.uds;

import java.util.Arrays;

public enum DataIdentifier {

    ISO_SAE_RESERVED(0x0000, 0x00FF, "ISO SAE Reserved"),
    VEHICLE_MANUFAC_SPECIFIC_1(0x0100, 0xA5FF, "Vehicle Manufacturer Specific"),
    LEGISLATIVE_USE_1(0xA600, 0xA7FF, "Reserved For Legislative Use"),
    VEHICLE_MANUFAC_SPECIFIC_2(0xA800, 0xACFF, "Vehicle Manufacturer Specific"),
    LEGISLATIVE_USE_2(0xAD00, 0xAFFF, "Reserved For Legislative Use"),
    VEHICLE_MANUFAC_SPECIFIC_3(0xB000, 0xB1FF, "Vehicle Manufacturer Specific"),
    LEGISLATIVE_USE_3(0xB200, 0xBFFF, "Reserved For Legislative Use"),
    VEHICLE_MANUFAC_SPECIFIC_4(0xC000, 0xC2FF, "Vehicle Manufacturer Specific"),
    LEGISLATIVE_USE_4(0xC300, 0xCEFF, "Reserved For Legislative Use"),
    VEHICLE_MANUFAC_SPECIFIC_5(0xCF00, 0xEFFF, "Vehicle Manufacturer Specific"),
    NETWORK_CONFIG_TRAC_TRAILER(0xF000, 0xF00F, "Network Configuration Data For Tractor Trailer Application Data Identifier"),
    VEHICLE_MANUFAC_SPECIFIC_6(0xF010, 0xF0FF, "Vehicle Manufacturer Specific"),
    VEHICLE_MANUFAC_SPECIFIC_7(0xF100, 0xF17F, "Identification Option Vehicle Manufacturer Specific Data Identifier"),
    BOOT_SOFTWARE_ID(0xF180, "Boot Software Identification Data Identifier"),
    APP_SOFTWARE_ID(0xF181, "Application Software Identification Data Identifier"),
    APP_DATA_ID(0xF182, "Application Data Identification Data Identifier"),
    BOOT_SOFTWARE_FINGERPRINT(0xF183, "Boot Software Finger"),
    APP_SOFTWARE_FINGERPRINT(0xF184, "Application Software Fingerprint Data Identifier"),
    APP_DATA_FINGERPRINT(0xF185, "Application Data Fingerprint Data Identifier"),
    DIAG_SESSION_DATA_ID(0xF186, "Active Diagnostic Session Data Identifier"),
    SPARE_PART_DATA_ID(0xF187, "Vehicle Manufacturer Spare Part Number Data Identifier"),
    ECU_SOFTWARE_NUM_ID(0xF188, "Vehicle Manufacturer ECU Software Number Data Identifier"),
    VEHICLE_1(0xF189, "Vehicle"),
    SYSTEM_SUPPLIER(0xF18A, "System Supplier"),
    ECU_MANUFACTURING(0xF18B, "ECU Manufacturing"),
    ECU_SERIAL(0xF18C, "ECU Serial"),
    SUPPORTED_FUNCTIONAL_UNITS(0xF18D, "Supported Functional Units Data Identifier"),
    VEHICLE_2(0xF18E, "Vehicle"),
    ISO_SAE_RESERVED_STANDARDIZED(0xF18F, "ISO SAE Reserved Standardized"),
    VIN(0xF190, "VIN Data Identifier"),
    ECU_HARDWARE_NUMBER(0xF191, "Vehicle Manufacturer ECU Hardware Number Data Identifier"),
    SYSTEM_SUPPLIER_ECU_HARDWARE_NUMBER(0xF192, "System Supplier ECU Hardware Number Data Identifier"),
    SYSTEM_SUPPLIER_ECU_HARDWARE_VERSION(0xF193, "System Supplier ECU Hardware Version Number Data Identifier"),
    SYSTEM_SUPPLIER_ECU_SOFTWARE_NUMBER(0xF194, "System Supplier ECU Software Number Data Identifier"),
    SYSTEM_SUPPLIER_ECU_SOFTWARE_VERSION(0xF195, "System Supplier ECU Software Version Number Data Identifier"),
    EXHAUST_REGULATION_APPROVAL_NUMBER(0xF196, "Exhaust Regulation Or Type Approval Number Data Identifier"),
    SYSTEM_NAME_ENGINE_TYPE(0xF197, "System Name Or Engine Type Data Identifier"),
    REPAIR_SHOP_CODE_TESTER_SERIAL_NUMBER(0xF198, "Repair Shop Code Or Tester Serial Number Data Identifier"),
    PROGRAMMING_DATE(0xF199, "Programming Date Data Identifier"),
    CALIBRATION_REPAIR_SHOP_CODE(0xF19A, "Calibration Repair Shop Code Or Calibration Equipment Serial Number"),
    CALIBRATION_DATE(0xF19B, "Calibration Date Data Identifier"),
    CALIBRATION_EQUIPMENT_SOFTWARE_NUMBER(0xF19C, "Calibration Equipment Software Number Data Identifier"),
    ECU_INSTALLATION_DATE(0xF19D, "ECU Installation Date Data Identifier"),
    ODX_FILE(0xF19E, "ODX File Data Identifier"),
    ENTITY_DATA(0xF19F, "Entity Data Identifier"),
    ID_OPTION_VEHICLE_MANUFAC_SPEC(0xF1A0, 0xF1EF, "Identification Option Vehicle Manufacturer Specific"),
    ID_OPTION_SYSTEM_SUPPLIER_SPEC(0xF1F0, 0xF1FF, "Identification Option System Supplier Specific"),
    PERIODIC_DATA(0xF200, 0xF2FF, "Periodic Data Identifier"),
    DYNAMICALLY_DEFINED(0xF300, 0xF3FF, "Dynamically Defined Data Identifier"),
    OBD_1(0xF400, 0xF4FF, "OBD Data Identifier"),
    OBD_2(0xF500, 0xF5FF, "OBD Data Identifier"),
    OBD_MONITOR_1(0xF600, 0xF6FF, "OBD Monitor Data Identifier"),
    OBD_MONITOR_2(0xF700, 0xF7FF, "OBD Monitor Data Identifier"),
    OBD_INFO_TYPE(0xF800, 0xF8FF, "OBD Info Type Data Identifier"),
    TACHOGRAPH(0xF900, 0xF9FF, "Tachograph Data Identifier"),
    AIRBAG_DEPLOYMENT(0xFA00, 0xFA0F, "Airbag Deployment Data Identifier"),
    NUM_EDR_DEVICES(0xFA10, "Number Of EDR Devices"),
    EDR_ID(0xFA11, "EDR Identification"),
    EDR_DEVICE_ADDRESS(0xFA12, "EDR Device Address Information"),
    EDR_ENTRIES(0xFA13, 0xFA18, "EDR Entries"),
    SAFETY_SYSTEM(0xFA19, 0xFAFF, "Safety System Data Identifier"),
    RESERVED_FUTURE_LEGISLATIVE(0xFB00, 0xFCFF, "Reserved For Future Legislative Requirements"),
    SYSTEM_SUPPLIER_SPECIFIC(0xFD00, 0xFEFF, "System Supplier Specific"),
    UDS_VERSION(0xFF00, "UDS Version Data Identifier"),
    ISO_SAE_RESERVED_2(0xFF01, 0xFFFF, "ISO SAE Reserved");

    private final short begin, end;
    private String text;

    DataIdentifier(int begin, String text) {
        this(begin, begin, text);
    }

    DataIdentifier(int begin, int end, String text) {
        this.begin = (short) begin;
        this.end = (short) end;
        this.text = text;
    }

    public static DataIdentifier findByDid(short did) {
        return Arrays.stream(values())
                .filter(entry -> {
                    int begin = entry.begin & 0xFFFF;
                    int end = entry.end & 0xFFFF;
                    int value = did & 0xFFFF;
                    return value >= begin && value <= end;
                })
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(String.format("Unknown DID %04X", did)));
    }

    public String text() {
        return text;
    }
}
