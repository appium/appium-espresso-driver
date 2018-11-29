package io.appium.espressoserver.lib.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class MobileBackdoorMethod {
    private String name;

    @Nullable
    private List<BackdoorMethodArgs> args;

    public String getName() {
        return name;
    }

    @Nullable
    public List<BackdoorMethodArgs> getArgs() {
        if (args == null) {
            return new ArrayList<>();
        }
        return args;
    }
}