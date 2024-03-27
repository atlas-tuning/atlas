package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.model.MemoryReference;
import com.github.manevolent.atlas.model.MemorySection;
import com.github.manevolent.atlas.model.MemoryType;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.component.toolbar.MemoryRegionListToolbar;
import com.github.manevolent.atlas.ui.settings.validation.ValidationSeverity;
import com.github.manevolent.atlas.ui.settings.validation.ValidationState;
import com.github.manevolent.atlas.ui.util.Layout;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryRegionListSettingPage extends AbstractSettingPage implements ListSelectionListener {
    private final Editor editor;
    private final Project project;

    private JList<MemorySection> list;
    private JPanel settingsContent;
    private JComponent content;
    private SettingPage settingPage;

    private java.util.Map<MemorySection, MemorySection> workingCopies = new HashMap<>();
    private java.util.Map<MemorySection, SettingPage> settingPages = new HashMap<>();

    protected MemoryRegionListSettingPage(Editor editor, Project project) {
        super(CarbonIcons.CHIP, "Memory Regions");

        this.editor = editor;
        this.project = project;

        getContent();
    }

    private ListModel<MemorySection> createListModel() {
        DefaultListModel<MemorySection> model = new DefaultListModel<>();

        project.getSections().stream()
                .map(section -> workingCopies.computeIfAbsent(section, MemorySection::copy))
                .sorted(Comparator.comparing(MemorySection::getBaseAddress))
                .forEach(model::addElement);

        return model;
    }

    private JList<MemorySection> initList() {
        JList<MemorySection> list = new JList<>(createListModel());
        Layout.emptyBorder(list);
        list.setBackground(new JPanel().getBackground());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (list.getModel().getSize() > 0) {
            list.setSelectedIndex(0);
        }

        list.addListSelectionListener(this);

        return list;
    }

    private JPanel initLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new MemoryRegionListToolbar(this).getComponent(), BorderLayout.NORTH);
        panel.add(list = initList(), BorderLayout.CENTER);

        return panel;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        updateSettingContent();
    }

    public MemorySection getRealSection(MemorySection workingCopy) {
        return workingCopies.keySet().stream()
                .filter(real -> workingCopies.get(real) == workingCopy)
                .findFirst().orElseThrow();
    }

    private void updateSettingContent() {
        settingsContent.removeAll();

        MemorySection selected = list.getSelectedValue();

        if (selected != null) {
            settingPage = settingPages.get(selected);

            if (settingPage == null) {
                settingPage = new MemoryRegionSettingPage(editor, project, getRealSection(selected), selected) {
                    @Override
                    public void reinitialize() {
                        apply(); // This is fine as we are using a copied region; we are only applying back to the copy

                        super.reinitialize();
                    }

                    @Override
                    public boolean apply() {
                        boolean applied = super.apply();

                        SwingUtilities.invokeLater(() -> {
                            int index = list.getSelectedIndex();
                            list.setModel(createListModel());
                            if (index >= 0) {
                                list.setSelectedIndex(index);
                            }
                        });

                        return applied;
                    }
                };

                settingPages.put(selected, settingPage);
            }

            settingsContent.add(settingPage.getContent(), BorderLayout.CENTER);
        }

        settingsContent.revalidate();
        settingsContent.repaint();
    }

    private JPanel initRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        Layout.emptyBorder(panel);
        return panel;
    }

    private JComponent initComponent() {
        JScrollPane leftScrollPane = new JScrollPane(
                initLeftPanel(),
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Layout.matteBorder(0, 0, 0, 1, Color.GRAY.darker(), leftScrollPane);

        JScrollPane rightScrollPane = new JScrollPane(
                settingsContent = initRightPanel(),
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        Layout.emptyBorder(rightScrollPane);

        updateSettingContent();

        return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
    }

    @Override
    public JComponent getContent() {
        if (content == null) {
            content = initComponent();
        }

        return content;
    }

    @Override
    public boolean apply() {
        boolean applied = settingPages.values().stream().allMatch(SettingPage::apply);
        if (applied) {
            workingCopies.forEach(MemorySection::apply);
        }
        return applied;
    }

    @Override
    public void validate(ValidationState validation) {
        long codeSections = workingCopies.values().stream()
                .filter(x -> x.getMemoryType() == MemoryType.CODE).count();

        if (codeSections <= 0) {
            validation.add(this, ValidationSeverity.ERROR, "At least one " +
                    MemoryType.CODE + " memory region must be defined.");
        } else if (codeSections > 1) {
            validation.add(this, ValidationSeverity.ERROR, "Only one " +
                    MemoryType.CODE + " memory region can be defined.");
        }

        workingCopies.forEach((real, workingCopy) -> {
            if (workingCopy.getName().isBlank()) {
                validation.add(this, ValidationSeverity.ERROR, "Memory region name cannot be left blank");
            }

            java.util.List<MemorySection> collisions = workingCopies.values().stream()
                    .filter(s -> s != workingCopy)
                    .filter(s -> s.intersects(workingCopy))
                    .toList();

            if (!collisions.isEmpty()) {
                validation.add(this, ValidationSeverity.ERROR, "Memory region " + workingCopy.getName() +
                        " would intersect with " +
                        "other defined memory regions.\r\n" +
                        "Regions: " + collisions.stream().map(MemorySection::getName).collect(Collectors.joining(", ")) + ".");
            }

            java.util.List<MemoryReference> references = project.getMemoryReferences().stream()
                    .filter(real::contains)
                    .toList();

            List<MemoryReference> broken = references.stream()
                    .filter(ref -> !workingCopy.contains(ref))
                    .sorted(Comparator.comparing(ref -> ref.getAddress().getOffset()))
                    .toList();

            if (!broken.isEmpty()) {
                validation.add(this, ValidationSeverity.ERROR, "Memory region " + workingCopy.getName()
                        + " would break " + broken.size()
                        + " memory reference(s):\r\n" +
                        broken.stream().limit(20)
                                .map(MemoryReference::toString)
                                .collect(Collectors.joining(", ")) + ".\r\n" +
                        "More references could become broken; only the first 20 will be shown.");
            }
        });
    }

    @Override
    public boolean isScrollNeeded() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return settingPages.values().stream().anyMatch(SettingPage::isDirty);
    }
}
