package com.github.manevolent.atlas;

import com.github.manevolent.atlas.model.Rom;
import com.github.manevolent.atlas.model.builtin.SubaruWRX2022MT;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RomTest {
    @Test
    public void test_LoadSave() throws IOException {
        Rom a = SubaruWRX2022MT.newRom();
        float cell_a = a.getTables().get(0).getCell(0, 0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        a.saveToArchive(baos);
        Rom b = Rom.loadFromArchive(new ByteArrayInputStream(baos.toByteArray()));
        float cell_b = b.getTables().get(0).getCell(0, 0);
        assertEquals(cell_a, cell_b, 0.01f);
    }
}
