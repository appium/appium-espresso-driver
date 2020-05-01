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
import androidx.test.espresso.*
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * Get a View in an the Android App
 * Hack solution that makes use of Espresso ViewActions
 */
class ViewGetter {
    private val views = arrayOf<View?>(null)

    /**
     * To get the root view we implement a custom ViewAction that simply takes the View
     * and then saves it to an array in it's parent class.
     */
    private inner class GetViewAction : ViewAction {
        override fun getConstraints(): Matcher<View> {
            // This is a hack constraint that passes any view through
            return object : TypeSafeMatcher<View>() {
                override fun describeTo(description: Description) {
                    description.appendText("always matches to: ")
                }

                override fun matchesSafely(item: View?): Boolean {
                    return true
                }
            }
        }

        override fun getDescription(): String {
            return "getting view in application"
        }

        override fun perform(uiController: UiController, view: View) {
            views[0] = view
        }
    }

    /**
     * This function calls the above view action which saves the view to 'views' array
     * and then returns it
     * @return The root
     */
    val rootView: View
        get() {
            Espresso.onView(ViewMatchers.isRoot()).perform(GetViewAction())
            return views[0]!!
        }

    fun getView(viewInteraction: ViewInteraction): View {
        viewInteraction.perform(GetViewAction())
        return views[0]!!
    }

    fun getView(dataInteraction: DataInteraction): View {
        dataInteraction.perform(GetViewAction())
        return views[0]!!
    }
}
