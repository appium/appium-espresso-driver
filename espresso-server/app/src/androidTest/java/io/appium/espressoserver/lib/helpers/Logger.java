package io.appium.espressoserver.lib.helpers;

public class Logger {
    private static final String TAG = "appium";

    private static String toString(Object... args) {
        final StringBuilder content = new StringBuilder();

        for (Object arg : args) {
            if (arg != null) {
                content.append(arg.toString());
            }
        }

        return content.toString();
    }

    public static void error(Object... messages) {
        android.util.Log.e(TAG, toString(messages));
    }

    public static void error(String message, Throwable throwable) {
        android.util.Log.e(TAG, toString(message), throwable);
    }

    public static void info(Object... messages) {
        android.util.Log.i(TAG, toString(messages));
    }

    public static void debug(Object... messages) {
        android.util.Log.d(TAG, toString(messages));
    }
}
