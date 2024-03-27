package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.ui.settings.field.*;
import com.github.manevolent.atlas.ui.settings.field.*;
import com.github.manevolent.atlas.ui.settings.validation.ValidationSeverity;
import com.github.manevolent.atlas.ui.settings.validation.ValidationState;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryRegionSettingPage extends BasicSettingPage {
    private final Frame parent;
    private final Project project;

    private final MemorySection real;
    private final MemorySection section;

    public MemoryRegionSettingPage(Frame parent, Project project,
                                   MemorySection real, MemorySection section) {
        super(parent, CarbonIcons.CHIP, "Memory Region - " + section.getName());

        this.project = project;
        this.parent = parent;
        this.real = real;
        this.section = section;
    }

    public MemorySection getRealSection() {
        return real;
    }

    public MemorySection getWorkingSection() {
        return section;
    }

    @Override
    protected List<SettingField<?>> createFields() {
        List<SettingField<?>> elements = new ArrayList<>();

        elements.add(new TextSettingField(
                "Name", "The name of this memory region",
                section.getName(),
                v -> true,
                section::setName
        ));

        elements.add(new EnumSettingField<>(
                "Type",
                "The memory type of this region",
                MemoryType.class,
                section.getMemoryType(),
                v -> true,
                section::setMemoryType
        ));

        elements.add(new AddressSettingField(
                "Base Address", "The base address of this memory region",
                section.getBaseAddress(),
                v -> true,
                section::setBaseAddress
        ));

        elements.add(new IntegerSettingField(
                "Length", "The data length of this memory region",
                section.getDataLength(),
                0,
                Integer.MAX_VALUE,
                v -> true,
                section::setDataLength
        ));

        elements.add(new EnumSettingField<>(
                "Byte Order",
                "The byte order for data in this region",
                MemoryByteOrder.class,
                section.getByteOrder(),
                v -> true,
                section::setByteOrder
        ));

        elements.add(new EnumSettingField<>(
                "Encryption Type",
                "The encryption type for data in this region",
                MemoryEncryptionType.class,
                section.getEncryptionType(),
                v -> true,
                v -> {
                    section.setEncryptionType(v);
                    reinitialize();
                }));

        MemoryEncryptionType encryptionType = section.getEncryptionType();
        if (encryptionType != null && encryptionType != MemoryEncryptionType.NONE) {
            for (PropertyDefinition parameter : encryptionType.getFactory().getPropertyDefinitions()) {
                elements.add(createSettingField(parameter, project));
            }
        }

        return elements;
    }

    @Override
    public boolean isDirty() {
        return !project.getSections().contains(real) || super.isDirty();
    }
}
