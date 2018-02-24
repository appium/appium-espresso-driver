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

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.model.Session;

import io.appium.espressoserver.lib.model.SessionParams;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

public class CreateSession implements RequestHandler<SessionParams, Session> {

    @Override
    public Session handle(SessionParams params) throws AppiumException {
        Session appiumSession = Session.createGlobalSession(params.getDesiredCapabilities());
        String activityName = params.getDesiredCapabilities().getAppActivity();
        try {
            if (activityName != null) { // TODO: Remove this, using it now for testing purposes
                startActivity(activityName);
            }
        } catch (RuntimeException e) {
            throw new SessionNotCreatedException(e.getMessage());
        }
        return appiumSession;
    }

    private void startActivity(String appActivity) {
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        final String target = String.format("Starting activity: %s/%s",
                mInstrumentation.getTargetContext().getClass().getName(), appActivity);
        Logger.info(String.format("Starting activity '%s'", target));
        mInstrumentation.getUiAutomation().executeShellCommand(
                String.format("am start -W -n %s"
                        + " -S -a android.intent.action.MAIN"
                        + " -c android.intent.category.LAUNCHER"
                        + " -f 0x10200000", target));
        Logger.info(String.format("Activity '%s' started", target));
    }
}
