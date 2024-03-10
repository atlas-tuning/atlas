package com.github.manevolent.atlas.settings;

import com.github.manevolent.atlas.ApplicationMetadata;
import com.github.manevolent.atlas.logging.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import java.io.*;

import java.util.logging.Level;

public final class Settings {
    private static final String SETTINGS_FOLDER_NAME = "." + ApplicationMetadata.getName().toLowerCase();

    public static File getSettingsDirectory() {
        String home = System.getProperty("user.home");
        File homeDirectory = new File(home);
        File settingsDirectory = new File(homeDirectory.getPath() + File.separator + SETTINGS_FOLDER_NAME);
        settingsDirectory.mkdirs();
        return settingsDirectory;
    }

    private static Settings settings;

    public static Settings getAll() {
        if (settings == null) {
            settings = new Settings(getSettingsDirectory());
        }
        return settings;
    }

    public static <V, R extends SettingValue<V>, T extends Setting<R>> V get(T setting) {
        return getAll().getValue(setting);
    }

    public static <V, R extends SettingValue<V>, T extends Setting<R>> V get(T setting, V defaultValue) {
        V value = getAll().getValue(setting);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }


    public static <V, R extends SettingValue<V>, T extends Setting<R>> void set(T setting, V value) {
        getAll().setValue(setting, value);
    }

    private final File directory;
    private final File settingsFile;
    private final JsonObject settingsMap;

    private Settings(File directory) {
        this.directory = directory;
        this.settingsFile = new File(directory.getPath() + File.separator + "settings.json");
        this.settingsMap = load();
    }

    public JsonObject load() {
        if (settingsFile.exists()) {
            try {
                return JsonParser.parseReader(new FileReader(settingsFile)).getAsJsonObject();
            } catch (FileNotFoundException e) {
                Log.settings().log(Level.SEVERE, "Problem loading settings", e);
            }
        }

        return new JsonObject();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(settingsFile)) {
            new Gson().toJson(settingsMap, new JsonWriter(writer));
            Log.settings().log(Level.FINE, "Saved settings to " + settingsFile.getPath() + ".");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonElement getValueElement(String settingName) {
        return settingsMap.get(settingName);
    }

    public <V, R extends SettingValue<V>, T extends Setting<R>> V getValue(T setting) {
        R valueClass = setting.getValueClass();
        JsonElement valueElement = getValueElement(setting.getName());
        if (valueElement == null) {
            return valueClass.getDefault();
        }
        return valueClass.fromJson(valueElement);
    }

    public <V, R extends SettingValue<V>, T extends Setting<R>> void setValue(T setting, V value) {
        R valueClass = setting.getValueClass();
        JsonElement element = valueClass.toJson(value);
        settingsMap.add(setting.getName(), element);
        Log.settings().log(Level.FINE, "Set setting " + setting.getName() + " to \"" + value.toString() + "\".");
        save();
    }

}
