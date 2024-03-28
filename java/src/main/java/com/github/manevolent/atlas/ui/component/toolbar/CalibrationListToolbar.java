package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.settings.CalibrationListSettingPage;
import com.github.manevolent.atlas.ui.settings.MemoryRegionListSettingPage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class CalibrationListToolbar extends Toolbar<CalibrationListSettingPage> {
    public CalibrationListToolbar(CalibrationListSettingPage settingPage) {
        super(settingPage);
    }

    @Override
    protected void preInitComponent(JToolBar toolbar) {
        super.preInitComponent(toolbar);

        toolbar.setOrientation(JToolBar.HORIZONTAL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY.darker()));
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(makeSmallButton(FontAwesomeSolid.PLUS, "new", "New calibration", _ -> {
            getParent().newCalibration();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.TRASH, "delete", "Delete calibration", _ -> {
            getParent().deleteCalibration();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.COPY, "copy", "Copy calibration", _ -> {
            getParent().copyCalibration();
        }));
    }
}
