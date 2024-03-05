package com.github.manevolent.atlas;

import com.github.manevolent.atlas.definition.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.github.manevolent.atlas.definition.Axis.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.manevolent.atlas.definition.builtin.SubaruWRX2022MT.*;

public class FlashTest {

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
    public void test_BoostError() {
        int target = 0x0917;
        int manifold = 0x229f;

        float target_psi = manifoldPressure_16bit.build().forward(target);
        float manifold_psi = manifoldPressure_16bit.build().forward(manifold);
        float calculated_error = target_psi - manifold_psi;
        float error_psi = boostErrorPressure_16bit.build().forward((short) 0xE678);

        assertEquals(calculated_error, error_psi, 0.0001f);
    }

    @Test
    public void test_wastegate_position() {
        int position = 0x4000;
        float mm = wastegatePosition16bitScale.build().forward(position);
        assertEquals(0.00f, mm, 0.00f);
    }

    @Test
    @Disabled
    public void test_WriteCsvs() throws IOException {
        Rom rom = newRom();
        for (Table table : rom.getTables()) {
            table.writeCsv(new FileOutputStream("tables/" + testRomName + "/" + table.getName() + ".csv"), 2);
        }
    }

    @Test
    public void test_Scaling() {
        Scale scale = baseIgnitionTiming.build();
        float example = 10;
        assertEquals(example, scale.reverse(scale.forward(example)), 0.01f);

        Scale otherScale = boostTargetPressureScale_RelSL_16bit.build();
        float forward = otherScale.reverse(example);
        assertEquals(example, otherScale.forward(forward), 0.01f);
    }

    @Test
    public void test_ReadWrite() throws IOException {
        Table table = newRom().getTables().stream().findFirst().orElseThrow(
                () -> new AssertionError("No tables")
        );

        float value_0 = table.getCell(0, 0);
        float value_1 = table.getCell(1, 0);
        float value_2 = table.getCell(2, 0);
        float value_3 = table.getCell(3, 0);

        float stored = table.setCell(value_3 + 1, 3, 0);

        assertEquals(value_0, table.getCell(0, 0));
        assertEquals(value_1, table.getCell(1, 0));
        assertEquals(value_2, table.getCell(2, 0));
        assertEquals(value_3 + 1, table.getCell(3, 0));

    }


}
