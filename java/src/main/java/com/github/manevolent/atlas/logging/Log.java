package com.github.manevolent.atlas.logging;

import java.util.logging.Logger;

public class Log {
    private static Logger logger;

    public static Logger get() {
        if (logger == null) {
            logger = Logger.getLogger("atlas");
        }

        return logger;
    }

}
