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

import android.content.pm.ActivityInfo

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.ViewElement

class GetOrientation : RequestHandler<AppiumParams, Int> {

    @Throws(AppiumException::class)
    override fun handle(params: AppiumParams): Int {
        val view = Element.getViewById(params.elementId)
        try {
            when (ViewElement(view).extractActivity()?.requestedOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else -> return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } catch (e: Exception) {
            throw AppiumException("Cannot get screen orientation", e)
        }

    }
}
