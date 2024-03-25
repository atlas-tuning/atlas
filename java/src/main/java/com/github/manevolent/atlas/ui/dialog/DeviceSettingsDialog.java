package com.github.manevolent.atlas.ui.dialog;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceType;
import com.github.manevolent.atlas.ui.util.*;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import static java.awt.event.ItemEvent.SELECTED;

public class DeviceSettingsDialog extends JDialog {
    private JComboBox<J2534DeviceType> deviceTypeField;
    private JComboBox<J2534DeviceDescriptor> deviceField;

    public DeviceSettingsDialog(JFrame parent) {
        setType(Type.POPUP);
        setIconImage(Icons.getImage(CarbonIcons.TOOL_BOX, Color.WHITE).getImage());
        initComponent();
        pack();
        setResizable(false);
        setModal(true);
        setLocationRelativeTo(parent);
        setTitle("Device Settings");

        setMinimumSize(new Dimension(350, 150));
    }

    private static JComboBox<J2534DeviceType> deviceTypeField(J2534DeviceType existing,
                                                              Consumer<J2534DeviceType> valueChanged) {
        J2534DeviceType[] values = Arrays.stream(J2534DeviceType.values())
                .sorted(Comparator.comparing(J2534DeviceType::toString)).toArray(J2534DeviceType[]::new);
        JComboBox<J2534DeviceType> comboBox = new JComboBox<>(values);

        comboBox.setSelectedItem(existing);
        comboBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED) {
                return;
            }
            valueChanged.accept((J2534DeviceType)e.getItem());
        });
        return comboBox;
    }

    private void updateDeviceModel(J2534DeviceProvider<?> provider) {
        List<J2534DeviceDescriptor> devices;

        try {
            devices = provider.getAllDevices().stream().map(
                    x -> (J2534DeviceDescriptor) x
            ).toList();
        } catch (Throwable e) {
            String message = "Problem getting devices";
            Log.can().log(Level.SEVERE, message, e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(getParent(), message + "!\r\n" + e.getMessage() + "\r\n"
                                + "See console output (F12) for more details.",
                        "Device Error",
                        JOptionPane.ERROR_MESSAGE);
            });
            devices = new ArrayList<>();
        }

        J2534DeviceDescriptor[] values = devices.stream()
                .sorted(Comparator.comparing(J2534DeviceDescriptor::toString))
                .toArray(J2534DeviceDescriptor[]::new);

        deviceField.setModel(new DefaultComboBoxModel<>(values));

        J2534DeviceDescriptor defaultDescriptor;
        try {
            defaultDescriptor = provider.getDefaultDevice();
        } catch (Exception ex) {
            if (devices.isEmpty()) {
                defaultDescriptor = null;
            } else {
                defaultDescriptor = devices.getFirst();
            }
        }

        deviceField.setSelectedItem(defaultDescriptor);

        deviceField.revalidate();
        deviceField.repaint();
    }

    private static JComboBox<J2534DeviceDescriptor> deviceField(Consumer<J2534DeviceDescriptor> valueChanged) {
        JComboBox<J2534DeviceDescriptor> comboBox = new JComboBox<>();
        comboBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED) {
                return;
            }
            valueChanged.accept((J2534DeviceDescriptor)e.getItem());
        });
        return comboBox;
    }

    private void initComponent() {
        JPanel frame = new JPanel();
        frame.setLayout(new GridBagLayout());
        frame.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.add(Layout.emptyBorder(0, 0, 10, 0, Labels.boldText("Device Settings")),
                Layout.gridBagConstraints(GridBagConstraints.NORTHWEST,
                        GridBagConstraints.NONE,
                        0, 0,
                        2, 1,
                        1, 1));

        Inputs.createEntryRow(frame, 1, "Type", "The type of J2534 device to establish a vehicle connection with",
                deviceTypeField = deviceTypeField(Devices.getType(), (newDeviceType) ->{
                    updateDeviceModel(newDeviceType.getProvider());
                    Devices.setType(newDeviceType);
                }));

        Inputs.createEntryRow(frame, 2, "Device", "The type of J2534 device to establish a vehicle connection with",
                deviceField = deviceField((newDevice) ->{
                    Devices.getProvider().setDefaultDevice(newDevice);
                }));

        updateDeviceModel(Devices.getProvider());

        frame.add(Inputs.button("OK", this::dispose),
                Layout.gridBagConstraints(GridBagConstraints.SOUTHEAST,
                        GridBagConstraints.NONE,
                        0, 3,
                        2, 1,
                        1, 1)
        );

        add(frame);
    }

}
