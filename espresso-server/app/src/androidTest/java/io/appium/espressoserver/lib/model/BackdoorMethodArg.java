package io.appium.espressoserver.lib.model;

@SuppressWarnings("unused")
public class BackdoorMethodArg {
    private String value;
    private String type;
    private Object parsedValue;
    private Class parsedType;

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

}
