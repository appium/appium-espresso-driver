package io.appium.espressoserver.lib.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.helpers.BackdoorUtils;

@SuppressWarnings("unused")
public class MobileBackdoorMethod {
    private String name;

    @Nullable
    private List<BackdoorMethodArg> args;

    public String getName() {
        return name;
    }

    @Nullable
    public List<BackdoorMethodArg> getRawArgs() {
        if (args == null) {
            return Collections.emptyList();
        } else {
            return args;
        }

    }

    public Class<?>[] getArgumentTypes() {
        List<BackdoorMethodArg> rawArgs = getRawArgs();
        Class<?>[] types = new Class<?>[rawArgs.size()];
        for (int i = 0; i < rawArgs.size(); i++) {
            types[i] = BackdoorUtils.parseType(getRawArgs().get(i).getType());
        }
        return types;
    }

    public List<Object> getArguments() {
        List<Object> values = new ArrayList<>();
        for (BackdoorMethodArg methodArg : getRawArgs()) {
            Object parsedValue = BackdoorUtils.parseValue(methodArg.getValue(), BackdoorUtils.parseType(methodArg.getType()));
            values.add(parsedValue);
        }
        return values;
    }
}