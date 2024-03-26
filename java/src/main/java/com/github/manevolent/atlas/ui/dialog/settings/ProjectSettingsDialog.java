package com.github.manevolent.atlas.ui.dialog.settings;

import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.dialog.settings.element.TextSettingField;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ProjectSettingsDialog extends SettingsDialog<Project> {
    private final Editor parent;

    public ProjectSettingsDialog(Editor parent, Project object) {
        super(CarbonIcons.PRODUCT, "Project Settings", parent, object);

        this.parent = parent;
    }

    @Override
    protected List<SettingPage> getPages() {
        return Arrays.asList(
                new DefaultSettingPage(CarbonIcons.CAR, "Vehicle",
                        new TextSettingField(
                                "Year", "The manufacture year of the vehicle",
                                getSettingObject().getVehicle().getYear(),
                                var -> getSettingObject().getVehicle().setYear(var)
                        ),
                        new TextSettingField(
                                "Market", "The market of the vehicle (i.e. USDM, JDM, EUDM)",
                                getSettingObject().getVehicle().getMarket(),
                                var -> getSettingObject().getVehicle().setMarket(var)
                        ),
                        new TextSettingField(
                                "Make", "The make of the vehicle (i.e. GM, Mitsubishi, Subaru)",
                                getSettingObject().getVehicle().getMake(),
                                var -> getSettingObject().getVehicle().setMake(var)
                        ),
                        new TextSettingField(
                                "Model", "The model of the vehicle (i.e. F150, EVO, WRX)",
                                getSettingObject().getVehicle().getModel(),
                                var -> getSettingObject().getVehicle().setModel(var)
                        ),
                        new TextSettingField(
                                "Trim", "The trim of the vehicle (i.e. Base, Premium, Limited)",
                                getSettingObject().getVehicle().getTrim(),
                                var -> getSettingObject().getVehicle().setTrim(var)
                        ),
                        new TextSettingField(
                                "Transmission", "The transmission of the vehicle (i.e. MT, AT, CVT, DCT)",
                                getSettingObject().getVehicle().getTransmission(),
                                var -> getSettingObject().getVehicle().setTransmission(var)
                        )
                ),
                new ConnectionSettingPage(parent, getSettingObject()),
                new DefaultSettingPage(CarbonIcons.CHIP, "Memory Regions")
        );
    }

    @Override
    protected boolean apply() {
        boolean applied = super.apply();
        parent.setDirty(true); // Dirty because any applications could still have succeeded
        return applied;
    }
}
