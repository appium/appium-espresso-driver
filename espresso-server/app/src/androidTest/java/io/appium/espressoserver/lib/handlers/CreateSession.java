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
import io.appium.espressoserver.lib.model.Session;
import io.appium.espressoserver.lib.model.SessionParams;

import static io.appium.espressoserver.lib.handlers.StartActivity.startActivity;


public class CreateSession implements RequestHandler<SessionParams, Session> {

    @Override
    public Session handle(SessionParams params) throws AppiumException {
        Session appiumSession = Session.createGlobalSession(params.getDesiredCapabilities());
        String activityName = params.getDesiredCapabilities().getAppActivity();
        try {
            if (activityName != null) {
                startActivity(activityName, true);
            }
        } catch (Exception e) {
            throw new SessionNotCreatedException(e);
        }
        return appiumSession;
    }
}
