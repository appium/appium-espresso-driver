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
import android.widget.AbsListView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

class ScrollTo : ViewAction {
    private var xOffset = 0
    private var yOffset = 0

    override fun getConstraints(): Matcher<View> {
        // This is a hack constraint that passes any view through
        return ViewMatchers.isDescendantOfA(ViewMatchers.isRoot())
    }

    override fun getDescription(): String {
        return "scrolling by [$xOffset, $yOffset] vector"
    }

    override fun perform(uiController: UiController, view: View) {
        val x = view.left + xOffset
        val y = view.top + view.height + yOffset
        val viewParent = view.parent as View
        if (viewParent is AbsListView) {
            viewParent.smoothScrollToPosition(y)
        } else {
            viewParent.scrollTo(x, y)
        }
    }
}
