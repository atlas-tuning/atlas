package com.github.manevolent.atlas.ui.dialog.settings;

import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.ui.dialog.settings.field.*;
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
    public boolean validate() {
        if (section.getName().isBlank()) {
            JOptionPane.showMessageDialog(parent,
                    "Memory region name cannot be left blank.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }

        List<MemorySection> collisions = project.getSections().stream()
                .filter(s -> s != real)
                .filter(s -> s.intersects(section))
                .toList();

        if (!collisions.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Memory region " + section.getName() + " would intersect with " +
                            "other defined memory regions:\r\n" +
                        collisions.stream().map(MemorySection::getName).collect(Collectors.joining(", ")) + ".",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }

        List<MemoryReference> references = project.getMemoryReferences().stream()
                .filter(real::contains)
                .toList();

        List<MemoryReference> broken = references.stream()
                .filter(ref -> !section.contains(ref))
                .sorted(Comparator.comparing(ref -> ref.getAddress().getOffset()))
                .toList();

        if (!broken.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Memory region " + section.getName()
                            + " would break " + broken.size()
                            + " memory reference(s):\r\n" +
                            broken.stream().limit(20)
                                    .map(MemoryReference::toString)
                                    .collect(Collectors.joining(", ")) + ".\r\n" +
                    "More references could become broken; only the first 20 will be shown.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
