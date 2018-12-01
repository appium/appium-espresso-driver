package io.appium.espressoserver.lib.model;

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
        }
        return args;
    }

    public void setArgs(List<BackdoorMethodArg> args) {
        this.args = args;
    }

    public Class<?>[] getArgumentTypes() {
        List<BackdoorMethodArg> rawArgs = getRawArgs();
        Class<?>[] types = new Class<?>[rawArgs.size()];
        for (int i = 0; i < rawArgs.size(); i++) {
            types[i] = BackdoorUtils.parseType(rawArgs.get(i).getType());
        }
        return types;
    }

    public Object[] getArguments() {
        List<BackdoorMethodArg> rawArgs = getRawArgs();
        Object[] parsedArgs = new Object[rawArgs.size()];
        for (int i = 0; i < rawArgs.size(); i++) {
            parsedArgs[i] = BackdoorUtils.parseValue(rawArgs.get(i).getValue(),
                    BackdoorUtils.parseType(rawArgs.get(i).getType()));
        }
        return parsedArgs;
    }
}