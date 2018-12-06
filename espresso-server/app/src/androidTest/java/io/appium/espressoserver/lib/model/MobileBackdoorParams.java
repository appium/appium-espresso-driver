package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class MobileBackdoorParams extends AppiumParams {
    private InvocationTarget target;
    private String elementId;

    private List<MobileBackdoorMethod> methods;

    public InvocationTarget getTarget() {
        return target;
    }

    public List<MobileBackdoorMethod> getMethods() {
        return methods;
    }

    public String getElementId() {
        return elementId;
    }

    public enum InvocationTarget {
        @SerializedName("activity")
        ACTIVITY,
        @SerializedName("application")
        APPLICATION,
        @SerializedName("element")
        ELEMENT,
    }

}

