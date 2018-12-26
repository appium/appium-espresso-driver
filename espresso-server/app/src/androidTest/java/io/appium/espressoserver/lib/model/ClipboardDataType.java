package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public enum ClipboardDataType {
    @SerializedName("PLAINTEXT")
    PLAINTEXT;

    public static String supportedDataTypes() {
        return Arrays.toString(values());
    }
}
