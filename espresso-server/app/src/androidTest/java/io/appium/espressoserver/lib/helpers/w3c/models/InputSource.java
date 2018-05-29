package io.appium.espressoserver.lib.helpers.w3c.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class InputSource {
    private String id;

    public String getId() {
        return id;
    }

    public static InputSource deserialize(final String inputJson) {
        JsonDeserializer<InputSource> deserializer = new JsonDeserializer<InputSource>() {
            @Override
            public InputSource deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                JsonElement type = jsonObject.get("type");

                if (type == null) {
                    // null input source type
                    return InputSource.class.cast((new Gson()).fromJson(inputJson, InputSource.class));
                }

                String typeString = type.getAsString();

                switch (typeString) {
                    case "pointer":
                        return PointerInputSource.class.cast((new Gson()).fromJson(inputJson, PointerInputSource.class));
                    case "key":
                        return KeyInputSource.class.cast((new Gson()).fromJson(inputJson, KeyInputSource.class));
                    default:
                        throw new JsonParseException(String.format("Invalid input source type %s", typeString));
                }
            }
        };

        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(InputSource.class, deserializer);

        Gson customGson = gsonBuilder.create();
        InputSource customObject = customGson.fromJson(inputJson, InputSource.class);
        return customObject;
    }
}
