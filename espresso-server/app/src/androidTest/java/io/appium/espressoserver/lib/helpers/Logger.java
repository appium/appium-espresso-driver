/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
