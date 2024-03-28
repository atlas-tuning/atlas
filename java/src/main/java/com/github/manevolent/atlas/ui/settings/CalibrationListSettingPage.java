package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.ui.Editor;

import com.github.manevolent.atlas.ui.component.toolbar.CalibrationListToolbar;
import com.github.manevolent.atlas.ui.settings.validation.ValidationSeverity;
import com.github.manevolent.atlas.ui.settings.validation.ValidationState;
import com.github.manevolent.atlas.ui.util.Layout;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

public class CalibrationListSettingPage extends AbstractSettingPage implements ListSelectionListener {
    private final Editor editor;
    private final Project project;

    private JList<Calibration> list;
    private JPanel settingsContent;
    private JComponent content;
    private CalibrationSettingPage settingPage;

    private java.util.Map<Calibration, Calibration> workingCopies = new HashMap<>();
    private java.util.Map<Calibration, CalibrationSettingPage> settingPages = new HashMap<>();

    protected CalibrationListSettingPage(Editor editor, Project project) {
        super(CarbonIcons.CATALOG, "Calibrations");

        this.editor = editor;
        this.project = project;

        project.getCalibrations().forEach(cal -> workingCopies.put(cal, cal.copy()));

        getContent();
    }

    private ListModel<Calibration> createListModel() {
        DefaultListModel<Calibration> model = new DefaultListModel<>();

        workingCopies.values()
                .stream().sorted(Comparator.comparing(Calibration::getName))
                .forEach(model::addElement);

        return model;
    }

    private JList<Calibration> initList() {
        JList<Calibration> list = new JList<>(createListModel());
        Layout.emptyBorder(list);
        list.setBackground(new JPanel().getBackground());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Layout.preferWidth(list, 200);

        if (list.getModel().getSize() > 0) {
            list.setSelectedIndex(0);
        }

        list.addListSelectionListener(this);

        return list;
    }

    private JPanel initLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new CalibrationListToolbar(this).getComponent(), BorderLayout.NORTH);
        panel.add(list = initList(), BorderLayout.CENTER);

        return panel;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        updateSettingContent();
    }

    public Calibration getRealCalibration(Calibration workingCopy) {
        return workingCopies.keySet().stream()
                .filter(real -> workingCopies.get(real) == workingCopy)
                .findFirst().orElseThrow();
    }

    private void updateSettingContent() {
        settingsContent.removeAll();

        Calibration selected = list.getSelectedValue();

        if (selected != null) {
            settingPage = settingPages.get(selected);

            if (settingPage == null) {
                settingPage = new CalibrationSettingPage(editor, project, getRealCalibration(selected), selected) {
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

    public void newCalibration() {
        MemorySection codeSection = project.getSections().stream()
                .filter(section -> section.getMemoryType() == MemoryType.CODE)
                .findFirst().orElse(null);

        if (codeSection == null) {
            JOptionPane.showMessageDialog(editor,
                    "Failed to create new calibration!\r\nYou must define a " + MemoryType.CODE + " memory region " +
                            "before creating a calibration.",
                    "Create failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newCalibrationName = (String) JOptionPane.showInputDialog(editor,
                "Specify a name", "New Calibration",
                QUESTION_MESSAGE, null, null, "New Calibration");

        if (newCalibrationName == null || newCalibrationName.isBlank()) {
            return;
        }

        Calibration calibration = Calibration.builder()
                .withName(newCalibrationName)
                .withReadOnly(false)
                .withSection(codeSection)
                .build();

        Calibration workingCopy = calibration.copy();

        workingCopies.put(calibration, workingCopy);

        SwingUtilities.invokeLater(() -> {
            // Update the model
            list.setModel(createListModel());
            list.setSelectedValue(workingCopy, true);
        });
    }

    public void copyCalibration() {
        if (settingPage == null) {
            return;
        }

        Calibration source = settingPage.getRealSection();

        String newCalibrationName = (String) JOptionPane.showInputDialog(editor,
                "Specify a name", "Copy Calibration",
                QUESTION_MESSAGE, null, null, source.getName() + " (Copy)");

        if (newCalibrationName == null || newCalibrationName.isBlank()) {
            return;
        }

        Calibration copy = source.copy();
        try {
            copy.dereferenceData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        copy.setName(newCalibrationName);
        Calibration workingCopy = copy.copy();
        workingCopies.put(copy, workingCopy);

        SwingUtilities.invokeLater(() -> {
            // Update the model
            list.setModel(createListModel());
            list.setSelectedValue(workingCopy, true);
        });
    }

    public void deleteCalibration() {
        if (settingPage == null) {
            return;
        }

        Calibration realCalibration = settingPage.getRealSection();
        Calibration workingCalibration = settingPage.getWorkingSection();

        if (JOptionPane.showConfirmDialog(editor,
                "WARNING!\r\nAre you sure you want to delete " + realCalibration.getName() + "?\r\n" +
                        "Doing so will PERMANENTLY remove any table data associated with this calibration.",
                "Delete Calibration",
                JOptionPane.YES_NO_OPTION,
                WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        settingPages.remove(workingCalibration);
        workingCopies.remove(realCalibration);

        SwingUtilities.invokeLater(() -> {
            // Update the model
            list.setModel(createListModel());
        });
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
        boolean applied = settingPages.values().stream().allMatch(BasicSettingPage::apply);

        if (applied) {
            workingCopies.forEach(Calibration::apply);

            workingCopies.forEach((real, workingCopy) -> {
                if (!project.getCalibrations().contains(real)) {
                    project.addCalibration(real);
                }
            });

            new ArrayList<>(project.getCalibrations())
                    .stream()
                    .filter(cal -> !workingCopies.containsKey(cal))
                    .forEach(project::removeCalibration);
        }

        return applied;
    }

    @Override
    public void validate(ValidationState validation) {
        if (workingCopies.isEmpty()) {
            validation.add(this, ValidationSeverity.WARNING, "No calibrations have been defined. Having at least one " +
                    "calibration is required to edit and define tables.");
        }

        workingCopies.values().stream().filter(calibration -> !calibration.hasData()).forEach(calibration -> {
            validation.add(this, ValidationSeverity.ERROR, "Calibration \"" + calibration.getName() + "\"" +
                    " does not have any backing ROM data. Make sure to supply a ROM file for this calibration, " +
                    "or copy another existing calibration first.");
        });
    }

    @Override
    public boolean isScrollNeeded() {
        return false;
    }

    @Override
    public boolean isDirty() {
        boolean willDelete = project.getCalibrations()
                .stream()
                .anyMatch(cal -> !workingCopies.containsKey(cal));

        return willDelete || settingPages.values().stream().anyMatch(SettingPage::isDirty);
    }
}
