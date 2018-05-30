package io.appium.espressoserver.lib.helpers.w3c.models.inputsources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

@SuppressWarnings("unused")
public class InputSource<BaseAction> {
    private InputSourceType type;
    private String id;
    private List<BaseAction> actions;

    public String getId() {
        return id;
    }

    public List<BaseAction> getActions() {
        return actions;
    }

    public enum InputSourceType {
        @SerializedName("pointer")
        POINTER,
        @SerializedName("key")
        KEY,
        @SerializedName("none")
        NONE;
    }
}
