package io.appium.espressoserver.lib.helpers.w3c.models.inputsources;

import com.google.gson.annotations.SerializedName;

public enum PointerTypeEnum {
    @SerializedName("mouse")
    MOUSE,
    @SerializedName("pen")
    PEN,
    @SerializedName("touch")
    TOUCH;
}
