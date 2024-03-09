package com.github.manevolent.atlas.logging;

import java.util.logging.Logger;

public class Log {
    private static Logger logger;
    private static Logger uiLogger;
    private static Logger canLogger;
    private static Logger settingsLogger;

    public static Logger get() {
        if (logger == null) {
            logger = Logger.getLogger("atlas");
        }

        return logger;
    }

    public static Logger ui() {
        if (uiLogger == null) {
            uiLogger = Logger.getLogger("ui");
            uiLogger.setParent(get());

        }
        return uiLogger;
    }

    public static Logger can() {
        if (canLogger == null) {
            canLogger = Logger.getLogger("can");
            canLogger.setParent(get());
        }
        return canLogger;
    }

    public static Logger settings() {
        if (settingsLogger == null) {
            settingsLogger = Logger.getLogger("settings");
            settingsLogger.setParent(get());
        }
        return settingsLogger;
    }

}
