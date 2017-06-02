package io.appium.espressoserver.lib.model.gsonadapters;

import io.appium.espressoserver.lib.model.AppiumStatus;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class AppiumStatusAdapter implements JsonSerializer<AppiumStatus> {

    @Override
    public JsonElement serialize(AppiumStatus status, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(status.getCode());
    }

}