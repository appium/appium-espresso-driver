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

package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import java.util.*

class Session private constructor(val sessionId: String, val capabilities: SessionParams.W3CCapabilities)
{
    companion object {
        // Only one session can run at a time so globally cache the current Session ID
        // Instances of Session are private and only returned by 'createGlobalSession'
        @Volatile
        var globalSession: Session? = null
            private set

        /**
         * Create a global session. Only one session can run per server instance so throw an exception
         * if one already is in progress
         *
         * @return Serializable Session object
         * @throws SessionNotCreatedException Thrown if a Session is already running
         */
        @Synchronized
        fun createGlobalSession(capabilities: SessionParams.W3CCapabilities): Session {
            globalSession?.let {
                AndroidLogger.logger.info("Got request for new session creation while the one " +
                        "is still in progress. Overriding the old session having the id ${it.sessionId}")
            }
            val globalSession = Session(UUID.randomUUID().toString(), capabilities)
            Session.globalSession = globalSession
            return globalSession
        }

        fun deleteGlobalSession() {
            globalSession = null
        }
    }
}
