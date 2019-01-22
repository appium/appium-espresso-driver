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

import javax.annotation.Nullable;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;

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

    private static String getFullyQualifiedActivityName(Instrumentation instrumentation, String activity) {
        final String pkg = instrumentation.getTargetContext().getPackageName();
        return activity.startsWith(".") ? pkg + activity : activity;
    }

    public static void startActivityViaInstruments(String activity, @Nullable String waitActivity, boolean waitForActivity) {
        logger.info(String.format("Starting activity '%s'", activity));
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        String fullyQualifiedAppActivity = getFullyQualifiedActivityName(instrumentation, activity);
        String fullyQualifiedWaitActivity = waitActivity == null
                ? fullyQualifiedAppActivity
                : getFullyQualifiedActivityName(instrumentation, waitActivity);
        Instrumentation.ActivityMonitor mSessionMonitor = instrumentation
                .addMonitor(fullyQualifiedWaitActivity, null, false);
        intent.setClassName(instrumentation.getTargetContext(), fullyQualifiedAppActivity);
        instrumentation.startActivitySync(intent);
        if (waitForActivity) {
            Activity mCurrentActivity = instrumentation.waitForMonitor(mSessionMonitor);
            logger.info(String.format("Activity '%s' started", mCurrentActivity.getLocalClassName()));
        }
    }

    public static void startActivityViaTestRule(String activity) throws AppiumException {
        logger.info(String.format("Starting activity '%s'", activity));
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final Class<? extends Activity> activityClass;
        try {
            //noinspection unchecked
            activityClass = (Class<? extends Activity>) Class
                    .forName(getFullyQualifiedActivityName(instrumentation, activity));
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new InvalidArgumentException(e);
        }
        ActivityTestRule activityTestRule = new ActivityTestRule<>(activityClass, true);
        activityTestRule.launchActivity(null);
        logger.info(String.format("Started '%s' activity", activityClass.getName()));
    }
}
