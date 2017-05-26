package io.appium.espressoserver.lib.Model;

/**
 * An emumeration of possible strategies.
 */
public enum AppiumStatus {
    // TODO: Add the rest of the Appium Statuses
    SUCCESS(0);

    private final int statusCode;

    private AppiumStatus(final int code) {
        statusCode = code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}