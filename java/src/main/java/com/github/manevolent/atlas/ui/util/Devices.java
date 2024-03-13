package com.github.manevolent.atlas.ui.util;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceType;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;

import java.util.logging.Level;

public class Devices {

    public static J2534DeviceType getType() {
        J2534DeviceType type = null;
        String providerSetting = Settings.get(Setting.DEVICE_PROVIDER);
        if (providerSetting != null) {
            try {
                type = J2534DeviceType.valueOf(providerSetting);
            } catch (Exception ex) {
                Log.ui().log(Level.WARNING, "Problem getting default J2534 device type", ex);
            }
        }

        if (type == null) {
            type = J2534DeviceType.TACTRIX_SERIAL_UNIX_SOCKET;
        }

        return type;
    }

    public static J2534DeviceProvider<?> getProvider() {
        return getType().getProvider();
    }

    public static void setType(J2534DeviceType newDeviceType) {
        Settings.set(Setting.DEVICE_PROVIDER, newDeviceType.name());
    }
}
