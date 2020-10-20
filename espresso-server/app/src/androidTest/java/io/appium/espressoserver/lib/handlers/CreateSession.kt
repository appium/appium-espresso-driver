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

package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException
import io.appium.espressoserver.lib.helpers.ActivityHelpers.startActivity
import io.appium.espressoserver.lib.helpers.NotificationListener
import io.appium.espressoserver.lib.helpers.w3c.caps.parseCapabilities
import io.appium.espressoserver.lib.model.GlobalSession
import io.appium.espressoserver.lib.model.SessionParams
import io.appium.espressoserver.lib.model.StartActivityParams


class CreateSession : RequestHandler<SessionParams, GlobalSession> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: SessionParams): GlobalSession {
        val appiumSession = GlobalSession.create(params.capabilities
                ?: throw SessionNotCreatedException("'capabilities' field is mandatory"))
        try {
            val parsedCaps = parseCapabilities(
                    params.capabilities.firstMatch,
                    params.capabilities.alwaysMatch
            )
            val shouldLaunchApp = (parsedCaps["autoLaunch"] ?: true) as Boolean
            if (shouldLaunchApp) {
                @Suppress("UNCHECKED_CAST")
                startActivity(StartActivityParams(
                        parsedCaps["appPackage"] as? String,
                        parsedCaps["appActivity"] as? String,
                        parsedCaps["appLocale"] as? Map<String, Any?>,
                        parsedCaps["intentOptions"] as? Map<String, Any?>,
                        parsedCaps["activityOptions"] as? Map<String, Any?>
                ))
            }
            NotificationListener.start()
        } catch (e: Exception) {
            appiumSession.delete()
            throw SessionNotCreatedException(e)
        }
        return appiumSession
    }
}
