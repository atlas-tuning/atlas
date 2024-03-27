package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.settings.field.TextSettingField;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

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
                new DefaultSettingPage(
                        parent,
                        CarbonIcons.CAR, "Vehicle",
                        new TextSettingField(
                                "Year", "The manufacture year of the vehicle",
                                getSettingObject().getVehicle().getYear(),
                                v -> true,
                                var -> getSettingObject().getVehicle().setYear(var)
                        ),
                        new TextSettingField(
                                "Market", "The market of the vehicle (i.e. USDM, JDM, EUDM)",
                                getSettingObject().getVehicle().getMarket(),
                                v -> true,
                                var -> getSettingObject().getVehicle().setMarket(var)
                        ),
                        new TextSettingField(
                                "Make", "The make of the vehicle (i.e. GM, Mitsubishi, Subaru)",
                                getSettingObject().getVehicle().getMake(),
                                v -> true,
                                var -> getSettingObject().getVehicle().setMake(var)
                        ),
                        new TextSettingField(
                                "Model", "The model of the vehicle (i.e. F150, EVO, WRX)",
                                getSettingObject().getVehicle().getModel(),
                                v -> true,
                                var -> getSettingObject().getVehicle().setModel(var)
                        ),
                        new TextSettingField(
                                "Trim", "The trim of the vehicle (i.e. Base, Premium, Limited)",
                                getSettingObject().getVehicle().getTrim(),
                                v -> true,
                                var -> getSettingObject().getVehicle().setTrim(var)
                        ),
                        new TextSettingField(
                                "Transmission", "The transmission of the vehicle (i.e. MT, AT, CVT, DCT)",
                                getSettingObject().getVehicle().getTransmission(),
                                v -> true,
                                var -> getSettingObject().getVehicle().setTransmission(var)
                        )
                ),
                new ConnectionSettingPage(parent, getSettingObject()),
                new MemoryRegionListSettingPage(parent, getSettingObject()),
                new DefaultSettingPage(parent, CarbonIcons.CATALOG, "Calibrations")
        );
    }

    @Override
    protected ApplyResult apply() {
        ApplyResult applied = super.apply();
        if (applied != ApplyResult.FAILED_VALIDATION && applied != ApplyResult.NOTHING_APPLIED) {
            parent.setDirty(true); // Dirty because any applications could still have succeeded
        }
        return applied;
    }
}
