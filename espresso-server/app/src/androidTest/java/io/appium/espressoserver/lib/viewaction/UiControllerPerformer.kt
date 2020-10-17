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

package io.appium.espressoserver.lib.viewaction

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.viewmatcher.withView
import org.hamcrest.Matcher

class UiControllerPerformer<T>(private val runnable: UiControllerRunnable<T>) : ViewAction {
    private var appiumException: AppiumException? = null
    private var runResult: T? = null

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isRoot()
    }

    override fun getDescription(): String {
        return "applying W3C actions "
    }

    override fun perform(uiController: UiController, view: View) {
        try {
            runResult = runnable.run(uiController)
        } catch (e: AppiumException) {
            appiumException = e
        }
    }

    @Throws(AppiumException::class)
    fun run(): T? {
        // Get the root view because it doesn't matter what we perform this interaction on
        val rootView = ViewGetter().rootView
        val viewInteraction = Espresso.onView(withView(rootView))
        AndroidLogger.info("Performing W3C actions sequence")
        try {
            viewInteraction.perform(this, ViewActions.closeSoftKeyboard())
        } catch (nme: NoMatchingViewException) {
            // Ignore this. The viewMatcher is a hack to begin with
        }
        if (appiumException != null) {
            throw appiumException!!
        }
        return runResult
    }
}
