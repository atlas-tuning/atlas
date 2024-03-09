package com.github.manevolent.atlas.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringValue implements SettingValue<String> {

    @Override
    public String fromJson(JsonElement element) {
        return element.getAsString();
    }

    @Override
    public JsonElement toJson(String value) {
        return new JsonPrimitive(value);
    }

}
