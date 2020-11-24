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

import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.ViewsCache
import io.appium.espressoserver.lib.helpers.extensions.withPermit
import java.util.*
import java.util.concurrent.Semaphore

object GlobalSession {
    private val SESSION_GUARD = Semaphore(1)
    @Suppress("ObjectPropertyName")
    private var _sessionId: String? = null
    @Suppress("ObjectPropertyName")
    private var _capabilities: SessionParams.W3CCapabilities? = null

    var sessionId: String?
        get() = SESSION_GUARD.withPermit { _sessionId }
        private set(value) = SESSION_GUARD.withPermit { _sessionId = value }
    var capabilities: SessionParams.W3CCapabilities?
        get () = SESSION_GUARD.withPermit { _capabilities }
        private set(value) = SESSION_GUARD.withPermit { _capabilities = value }
    val exists: Boolean
        get() = SESSION_GUARD.withPermit { _sessionId != null }
    
    fun create(capabilities: SessionParams.W3CCapabilities): GlobalSession {
        return SESSION_GUARD.withPermit {
            _sessionId?.let {
                AndroidLogger.warn("Got request for new session creation while the one " +
                        "is still in progress. Overriding the old session having the id $_sessionId")
                ViewsCache.reset()
            }
            _sessionId = UUID.randomUUID().toString()
            _capabilities = capabilities
            this
        }
    }

    fun delete() {
        SESSION_GUARD.withPermit {
            _sessionId = null
            _capabilities = null
            ViewsCache.reset()
        }
    }
}
