package io.appium.espressoserver.lib.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class MobileBackdoorMethod {
    private String name;

    @Nullable
    private List<Object> args;

    public String getName() {
        return name;
    }

    @Nullable
    public List<Object> getArgs() {
        List<Object> list = new ArrayList<>();

        if (args == null) {
            return list;
        }

        for (Object object : args) {
            // Convert floats back to integers because Gson parses integers as float.
            // https://stackoverflow.com/questions/15507997/how-to-prevent-gson-from-expressing-integers-as-floats/15508288
            if (object instanceof Double) {
                Double d = (Double) object;
                if ((d % 1) != 0) {
                    list.add(d);
                    continue;
                }
                if (d > Integer.MAX_VALUE) {
                    list.add(d.longValue());
                } else {
                    list.add(d.intValue());
                }

            } else {
                list.add(object);
            }
        }

        return list;
    }
}
