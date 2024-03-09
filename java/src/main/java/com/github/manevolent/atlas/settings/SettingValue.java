package com.github.manevolent.atlas.settings;

import com.google.gson.JsonElement;

public interface SettingValue<T> {

    default T getDefault() {
        return (T) null;
    }

    T fromJson(JsonElement element);

    JsonElement toJson(T value);

}
