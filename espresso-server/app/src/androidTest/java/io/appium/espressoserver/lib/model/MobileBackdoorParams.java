package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class MobileBackdoorParams extends AppiumParams {
    private InvokeTarget target;

    private List<MobileBackdoorMethod> methods;

    public InvokeTarget getTarget() {
        return target;
    }

    public List<MobileBackdoorMethod> getMethods() {
        return methods;
    }

    public enum InvokeTarget {
        @SerializedName("activity")
        ACTIVITY,
        @SerializedName("application")
        APPLICATION,
    }

}

