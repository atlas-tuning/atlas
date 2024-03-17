package com.github.manevolent.atlas;

import com.github.manevolent.atlas.logging.Log;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

public class ApplicationMetadata {

    private static final String resourceName = "/application.properties";

    private static Properties properties;

    private static Properties get() {
        if (properties == null) {
            Properties properties = new Properties();
            try {
                properties.load(ApplicationMetadata.class.getResourceAsStream(resourceName));
            } catch (Exception e) {
                Log.get().log(Level.WARNING, "Problem reading application metadata", e);
            }

            ApplicationMetadata.properties = properties;
        }

        return (properties);
    }

    public static String getName() {
        return get().getProperty("name");
    }

    public static String getVersion() {
        return get().getProperty("version");
    }

}
