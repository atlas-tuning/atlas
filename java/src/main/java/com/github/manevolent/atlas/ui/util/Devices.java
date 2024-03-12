package com.github.manevolent.atlas.ui.util;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceType;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;

public class Devices {

    public static J2534DeviceType getType() {
        J2534DeviceType type;
        String providerSetting = Settings.get(Setting.DEVICE_PROVIDER);
        if (providerSetting != null) {
            type = J2534DeviceType.valueOf(providerSetting);
        } else {
            type = J2534DeviceType.TACTRIX_SERIAL_UNIX_SOCKET;
        }
        return type;
    }

    public static J2534DeviceProvider getProvider() {
        return getType().getProvider();
    }

}
