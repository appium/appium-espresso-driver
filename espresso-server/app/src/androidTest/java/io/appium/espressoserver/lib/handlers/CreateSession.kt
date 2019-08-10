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
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException
import io.appium.espressoserver.lib.helpers.ActivityHelper.startActivity
import io.appium.espressoserver.lib.helpers.w3c.caps.parseCapabilities
import io.appium.espressoserver.lib.model.Session
import io.appium.espressoserver.lib.model.SessionParams


class CreateSession : RequestHandler<SessionParams, Session> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: SessionParams): Session {
        val appiumSession = Session.createGlobalSession(params.w3CCapabilities)
        val parsedCaps = parseCapabilities(params.w3CCapabilities.firstMatch, params.w3CCapabilities.alwaysMatch)
        val activityName = parsedCaps["appActivity"] as? String
                ?: throw SessionNotCreatedException(InvalidArgumentException("appActivity capability is mandatory"))
        try {
            startActivity(parsedCaps["appPackage"] as? String, activityName)
        } catch (e: Exception) {
            throw SessionNotCreatedException(e)
        }

        return appiumSession
    }
}
