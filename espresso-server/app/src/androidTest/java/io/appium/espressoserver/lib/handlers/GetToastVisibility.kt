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

import java.util.regex.Pattern

import androidx.test.espresso.EspressoException
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.ToastLookupParams
import io.appium.espressoserver.lib.viewmatcher.ToastMatcher

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.appium.espressoserver.lib.viewmatcher.withRegexp

class GetToastVisibility : RequestHandler<ToastLookupParams, Boolean> {
    @Throws(AppiumException::class)
    override fun handleInternal(params: ToastLookupParams): Boolean {
        try {
            val viewInteraction = if (params.isRegexp)
                onView(withRegexp(Pattern.compile(params.text))).inRoot(ToastMatcher())
            else
                onView(withText(params.text)).inRoot(ToastMatcher())
            viewInteraction.check(matches(isDisplayed()))
            return true
        } catch (e: Exception) {
            if (e is EspressoException) {
                return false
            }
            throw e
        }

    }
}
