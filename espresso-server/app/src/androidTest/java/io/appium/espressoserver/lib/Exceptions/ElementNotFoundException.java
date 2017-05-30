package io.appium.espressoserver.lib.Exceptions;

@SuppressWarnings("serial")
public class ElementNotFoundException extends Exception {
    private final static String error = "Could not find an element using supplied strategy. ";

    public ElementNotFoundException() {
        super(error);
    }

    public ElementNotFoundException(final String extra) {
        super(error + extra);
    }
}