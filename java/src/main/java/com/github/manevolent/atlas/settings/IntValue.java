package com.github.manevolent.atlas.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntValue implements SettingValue<Integer> {

    @Override
    public Integer fromJson(JsonElement element) {
        return element.getAsInt();
    }

    @Override
    public JsonElement toJson(Integer value) {
        return new JsonPrimitive(value);
    }

}
