package io.appium.espressoserver.lib.helpers.w3c.models;

import com.google.gson.annotations.JsonAdapter;

import javax.annotation.Nullable;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.VIEWPORT;

@JsonAdapter(OriginDeserializer.class)
public class Origin {
    private String type = VIEWPORT;
    private String elementId;

    public Origin() {
    }

    public Origin(String type) {
        this.type = type;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
}
