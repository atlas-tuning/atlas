package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.model.Calibration;
import com.github.manevolent.atlas.ui.component.window.TableEditor;
import com.github.manevolent.atlas.ui.util.Layout;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

public class TableEditorToolbar extends Toolbar<TableEditor> {
    public TableEditorToolbar(TableEditor editor) {
        super(editor);
    }

    private JComboBox<Calibration> initCalibrationsList() {
        DefaultComboBoxModel<Calibration> model = new DefaultComboBoxModel<>();
        getParent().getParent().getProject().getCalibrations().stream()
                .sorted(Comparator.comparing(Calibration::getName))
                .forEach(model::addElement);
        JComboBox<Calibration> calibrations = new JComboBox<>(model);
        calibrations.setSelectedItem(getParent().getCalibration());
        calibrations.addItemListener(e -> getParent().setCalibration((Calibration) e.getItem()));
        Layout.preferWidth(calibrations, 150);
        calibrations.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        calibrations.setFocusable(false);
        return calibrations;
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(makeButton(CarbonIcons.CHART_CUSTOM, "define", "Edit table definition", (e) -> {
            getParent().getParent().openTableDefinition(getParent().getTable());
        }));
        toolbar.addSeparator();
        toolbar.add(makeButton(FontAwesomeSolid.CALCULATOR, "calc", "Run custom function"));
        toolbar.addSeparator();
        toolbar.add(makeButton(FontAwesomeSolid.TIMES, "multiply", "Multiply value at selected cells",
                (e) -> getParent().scaleSelection()));
        toolbar.add(makeButton(FontAwesomeSolid.DIVIDE, "divide", "Divide value at selected cells",
                (e) -> getParent().divideSelection()));
        toolbar.add(makeButton(FontAwesomeSolid.PLUS, "add", "Add value to selected cells",
                (e) -> getParent().addSelection()));
        toolbar.addSeparator();
        toolbar.add(makeButton(FontAwesomeSolid.PERCENTAGE, "percent", "Scale values with a percentage",
                (e) -> getParent().scaleSelection()));
        toolbar.add(makeButton(FontAwesomeSolid.EQUALS, "average", "Average values in selection",
                (e) -> getParent().averageSelection()));
        toolbar.addSeparator();
        toolbar.add(makeButton(FontAwesomeSolid.GRIP_HORIZONTAL, "interpolate-horizontal", "Interpolate horizontally",
                (e) -> getParent().interpolateHorizontal()));
        toolbar.add(makeButton(FontAwesomeSolid.GRIP_VERTICAL, "interpolate-vertical", "Interpolate vertically",
                (e) -> getParent().interpolateVertical()));
        toolbar.addSeparator();

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add(initCalibrationsList());

    }
}
