package io.appium.espressoserver.lib.Exceptions;

/**
 * Created by ahmetkocu on 27.05.2017.
 */

@SuppressWarnings("serial")
public class ElementNotFoundException extends Exception {
    final static String error = "Could not find an element using supplied strategy. ";

    public ElementNotFoundException() {
        super(error);
    }

    public ElementNotFoundException(final String extra) {
        super(error + extra);
    }
}