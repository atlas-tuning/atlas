package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.ui.settings.field.*;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CalibrationSettingPage extends BasicSettingPage {
    private final Frame parent;
    private final Project project;

    private final Calibration real;
    private final Calibration calibration;

    public CalibrationSettingPage(Frame parent, Project project,
                                  Calibration real, Calibration calibration) {
        super(parent, CarbonIcons.CATALOG, "Calibration - " + calibration.getName());

        this.project = project;
        this.parent = parent;
        this.real = real;
        this.calibration = calibration;
    }

    public Calibration getRealSection() {
        return real;
    }

    public Calibration getWorkingSection() {
        return calibration;
    }

    @Override
    protected List<SettingField<?>> createFields() {
        List<SettingField<?>> elements = new ArrayList<>();

        elements.add(new TextSettingField(
                "Name", "The name of this calibration",
                calibration.getName(),
                v -> true,
                calibration::setName
        ));

        elements.add(new CheckboxSettingField(
                "Read-only", "Check if this calibration should not be edited in any table editors, etc.",
                calibration.isReadonly(),
                v -> true,
                calibration::setReadonly
        ));

        elements.add(new CalibrationField(
                "",
                calibration,
                () -> {}
        ));

        return elements;
    }

    @Override
    public boolean isDirty() {
        return !project.getCalibrations().contains(real) || super.isDirty();
    }
}
