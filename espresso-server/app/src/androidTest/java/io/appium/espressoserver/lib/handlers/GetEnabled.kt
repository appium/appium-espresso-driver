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

import junit.framework.AssertionFailedError

import androidx.test.espresso.NoMatchingViewException
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.Element

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled

class GetEnabled : RequestHandler<AppiumParams, Boolean> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: AppiumParams): Boolean {
        val viewInteraction = Element.getViewInteractionById(params.elementId)
        return try {
            viewInteraction.check(matches(isEnabled()))
            true
        } catch (e: NoMatchingViewException) {
            false
        } catch (e: AssertionFailedError) {
            false
        }

    }
}
