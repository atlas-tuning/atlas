package com.github.manevolent.atlas.ui.dialog.settings;

import com.github.manevolent.atlas.model.Project;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ProjectSettingsDialog extends SettingsDialog<Project> {
    public ProjectSettingsDialog(Frame parent, Project object) {
        super(CarbonIcons.PRODUCT, "Project Settings", parent, object);
    }

    @Override
    protected List<SettingPage> getPages() {
        return Arrays.asList(
                new BasicSettingPage(CarbonIcons.CAR, "Vehicle",
                        new BasicSettingPage.TextElement(
                                "Year", "The manufacture year of the vehicle",
                                getSettingObject().getVehicle().getYear(),
                                var -> getSettingObject().getVehicle().setYear(var)
                        ),
                        new BasicSettingPage.TextElement(
                                "Market", "The market of the vehicle (i.e. USDM, JDM, EUDM)",
                                getSettingObject().getVehicle().getMarket(),
                                var -> getSettingObject().getVehicle().setMarket(var)
                        ),
                        new BasicSettingPage.TextElement(
                                "Make", "The make of the vehicle (i.e. GM, Mitsubishi, Subaru)",
                                getSettingObject().getVehicle().getMake(),
                                var -> getSettingObject().getVehicle().setMake(var)
                        ),
                        new BasicSettingPage.TextElement(
                                "Model", "The model of the vehicle (i.e. F150, EVO, WRX)",
                                getSettingObject().getVehicle().getModel(),
                                var -> getSettingObject().getVehicle().setModel(var)
                        ),
                        new BasicSettingPage.TextElement(
                                "Trim", "The trim of the vehicle (i.e. Base, Premium, Limited)",
                                getSettingObject().getVehicle().getTrim(),
                                var -> getSettingObject().getVehicle().setTrim(var)
                        ),
                        new BasicSettingPage.TextElement(
                                "Transmission", "The transmission of the vehicle (i.e. MT, AT, CVT, DCT)",
                                getSettingObject().getVehicle().getTransmission(),
                                var -> getSettingObject().getVehicle().setTransmission(var)
                        )
                ),
                new Connection(),
                new Parameters()
        );
    }

    private class Connection extends AbstractSettingPage {
        protected Connection() {
            super(CarbonIcons.PLUG_FILLED, "Connection");
        }

        @Override
        public JComponent getContent() {
            return new JPanel();
        }

        @Override
        public void apply() {

        }
    }

    private class Parameters extends AbstractSettingPage {
        protected Parameters() {
            super(CarbonIcons.SETTINGS, "Parameters");
        }

        @Override
        public JComponent getContent() {
            return new JPanel();
        }

        @Override
        public void apply() {

        }
    }
}
