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

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.util.ArrayMap;

import java.lang.reflect.Field;

import androidx.test.platform.app.InstrumentationRegistry;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

public class ActivityHelper {
    //    https://androidreclib.wordpress.com/2014/11/22/getting-the-current-activity/
    public static Activity getCurrentActivity() throws AppiumException {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            //noinspection unchecked
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            ArrayMap activities = (ArrayMap) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            throw new AppiumException(e);
        }

        throw new AppiumException("Failed to get current Activity");
    }

    private static String getFullyQualifiedActivityName(Instrumentation instrumentation,
                                                        @Nullable String pkg, String activity) {
        if (pkg == null) {
            pkg = instrumentation.getTargetContext().getPackageName();
        }
        return activity.startsWith(".") ? pkg + activity : activity;
    }

    public static void startActivity(@Nullable String pkg, String activity) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        String fullyQualifiedAppActivity = getFullyQualifiedActivityName(instrumentation, pkg, activity);
        logger.info(String.format("Starting activity '%s'", activity));
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(instrumentation.getTargetContext(), fullyQualifiedAppActivity);
        instrumentation.startActivitySync(intent);
    }
}
