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

package io.appium.espressoserver.lib.handlers;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.StartActivityParams;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

public class StartActivity implements RequestHandler<StartActivityParams, Void> {

    @Override
    public Void handle(StartActivityParams params) throws AppiumException {
        StartActivity.startActivity(params.getAppActivity(), false);
        return null;
    }

    /**
     * Start an activity with provided activity name
     * @param appActivity
     * @param waitForActivity
     */
    public static void startActivity(String appActivity, Boolean waitForActivity) {
        logger.info(String.format("Starting activity '%s'", appActivity));
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor mSessionMonitor = mInstrumentation.addMonitor(appActivity, null, false);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Context context = mInstrumentation.getTargetContext();

        // If it's not fully qualified, make it fully qualified
        String fullyQualifiedAppActivity = appActivity;
        if (appActivity.startsWith(".")) {
            fullyQualifiedAppActivity = context.getPackageName() + appActivity;
        }
        intent.setClassName(mInstrumentation.getTargetContext(), fullyQualifiedAppActivity);
        mInstrumentation.startActivitySync(intent);

        if (waitForActivity) {
            Activity mCurrentActivity = mInstrumentation.waitForMonitor(mSessionMonitor);
            logger.info(String.format("Activity '%s' started", mCurrentActivity.getLocalClassName()));
        }
    }
}
