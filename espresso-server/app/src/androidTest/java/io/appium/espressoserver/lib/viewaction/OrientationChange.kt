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

import android.content.pm.ActivityInfo
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import io.appium.espressoserver.lib.model.ViewElement
import org.hamcrest.Matcher

class OrientationChange constructor(private val orientation: Int) : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isRoot()
    }

    override fun getDescription(): String {
        return "change orientation to $orientation"
    }

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()
        ViewElement(view).extractActivity()!!.requestedOrientation = orientation
    }
}

fun orientationLandscape(): ViewAction {
    return OrientationChange(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
}

fun orientationPortrait(): ViewAction {
    return OrientationChange(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
}
