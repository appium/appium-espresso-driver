package io.appium.espressoserver.lib.model;

@SuppressWarnings("unused")
public class BackdoorMethodArgs {
    private String value;
    private String type;
    private Object parsedValue;
    private Class parsedType;

    public Object getParsedValue() {
        return parsedValue;
    }

    public void setParsedValue(Object parsedValue) {
        this.parsedValue = parsedValue;
    }

    public Class getParsedType() {
        return parsedType;
    }

    public void setParsedType(Class parsedType) {
        this.parsedType = parsedType;
    }


    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

}
