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

package io.appium.espressoserver.lib.model;

import java.util.UUID;

import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.model.SessionParams.DesiredCapabilities;

@SuppressWarnings("unused")
public class Session {
    // Only one session can run at a time so globally cache the current Session ID
    private static volatile Session globalSession;

    private final String id;
    private final DesiredCapabilities desiredCapabilities;

    private Session(String id, DesiredCapabilities desiredCapabilities) {
        // Instances of Session are private and only returned by 'createGlobalSession'
        this.id = id;
        this.desiredCapabilities = desiredCapabilities;
    }

    public String getId() {
        return id;
    }

    public DesiredCapabilities getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public static Session getGlobalSession() {
        return globalSession;
    }

    /**
     * Create a global session. Only one session can run per server instance so throw an exception
     * if one already is in progress
     *
     * @return Serializable Session object
     * @throws SessionNotCreatedException Thrown if a Session is already running
     */
    public synchronized static Session createGlobalSession(DesiredCapabilities desiredCapabilities)
            throws SessionNotCreatedException {
        if (globalSession != null) {
            Logger.info(String.format("Got request for new session creation while the one " +
                            "is still in progress. Overriding the old session having id %s",
                    globalSession.getId()));
        }
        String globalSessionId = UUID.randomUUID().toString();
        Session.globalSession = new Session(globalSessionId, desiredCapabilities);
        return Session.globalSession;
    }

    public static void deleteGlobalSession() {
        Session.globalSession = null;
    }
}
