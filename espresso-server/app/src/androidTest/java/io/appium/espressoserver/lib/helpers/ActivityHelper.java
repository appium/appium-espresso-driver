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
import android.content.Context;
import android.content.Intent;
import android.util.ArrayMap;

import java.lang.reflect.Field;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
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

    private static String getFullyQualifiedActivityName(Instrumentation mInstrumentation, String appActivity) {
        Context context = mInstrumentation.getTargetContext();
        // If it's not fully qualified, make it fully qualified
        return appActivity.startsWith(".")
                ? context.getPackageName() + appActivity
                : appActivity;
    }

    public static void startActivityViaInstruments(String appActivity, boolean waitForActivity) {
        logger.info(String.format("Starting activity '%s'", appActivity));
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor mSessionMonitor = mInstrumentation.addMonitor(appActivity, null, false);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String fullyQualifiedAppActivity = getFullyQualifiedActivityName(mInstrumentation, appActivity);
        intent.setClassName(mInstrumentation.getTargetContext(), fullyQualifiedAppActivity);
        mInstrumentation.startActivitySync(intent);
        if (waitForActivity) {
            Activity mCurrentActivity = mInstrumentation.waitForMonitor(mSessionMonitor);
            logger.info(String.format("Activity '%s' started", mCurrentActivity.getLocalClassName()));
        }
    }

    public static void startActivityViaScenario(String activity) throws AppiumException {
        logger.info(String.format("Starting activity '%s'", activity));
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        final Class<? extends Activity> activityClass;
        try {
            //noinspection unchecked
            activityClass = (Class<? extends Activity>) Class
                    .forName(getFullyQualifiedActivityName(mInstrumentation, activity));
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new AppiumException(e);
        }
        ActivityScenario.launch(activityClass);
        logger.info(String.format("Activity '%s' started", activityClass.getName()));
    }
}
