package io.appium.espressoserver.lib.helpers.w3c.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action.ELEMENT_CODE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.VIEWPORT;

public class OriginDeserializer implements JsonDeserializer<Origin> {

    @Override
    public Origin deserialize(JsonElement json, Type paramType,
                                   JsonDeserializationContext paramJsonDeserializationContext) throws JsonParseException {
        Origin origin = new Origin();

        if (json.isJsonPrimitive()) {
            origin.setType(json.getAsString());
        } else if(json.isJsonObject() &&
                json.getAsJsonObject().get(ELEMENT_CODE).isJsonPrimitive()) {
            String elementId = json.getAsJsonObject().get(ELEMENT_CODE).getAsString();
            origin.setElementId(elementId);
            origin.setType(ELEMENT_CODE);
        } else if (json.isJsonNull()) {
            origin.setType(VIEWPORT);
        }

        return origin;
    }
}
