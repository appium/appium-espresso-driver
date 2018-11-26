package io.appium.espressoserver.lib.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class MobileBackdoorMethod {
    String name;

    @Nullable
    List<Object> args;

    public String getName() {
        return name;
    }

    @Nullable
    public List<Object> getArgs() {
        if (args == null) {
            return null;
        }

        List<Object> list = new ArrayList<>();

        for (Object object : args) {
            if (object instanceof Double) {
                Double d = (Double) object;
                if ((d % 1) == 0) {
                    list.add(d.intValue());
                } else {
                    list.add(d);
                }

            } else {
                list.add(object);
            }
        }

        return list;
    }
}
