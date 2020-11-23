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

import android.view.View
import android.view.ViewParent
import android.widget.AdapterView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.EspressoException
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.google.gson.annotations.SerializedName
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException
import io.appium.espressoserver.lib.helpers.StringHelpers.charSequenceToNullableString
import io.appium.espressoserver.lib.helpers.ViewState
import io.appium.espressoserver.lib.helpers.ViewsCache
import io.appium.espressoserver.lib.viewaction.ViewGetter
import io.appium.espressoserver.lib.viewmatcher.withView
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasEntry
import java.util.*

const val W3C_ELEMENT_KEY = "element-6066-11e4-a52e-4f735466cecf"
const val JSONWP_ELEMENT_KEY = "ELEMENT"

class Element(view: View) {
    @Suppress("JoinDeclarationAndAssignment")
    @SerializedName(JSONWP_ELEMENT_KEY, alternate = [W3C_ELEMENT_KEY])
    val element: String

    init {
        element = UUID.randomUUID().toString()
        ViewsCache.put(element, view)
    }

    companion object {
        /**
         * Retrieve cached view and return the ViewInteraction
         */
        @Throws(AppiumException::class)
        fun getViewInteractionById(elementId: String?): ViewInteraction {
            val view = getViewById(elementId)
            return onView(withView(view))
        }

        /**
         * This is a special case:
         *
         * If the contentDescription of a cached view changed, it almost certainly means the rendering of an
         * AdapterView (ListView, GridView, Scroll, etc...) changed and the View contents were shuffled around.
         *
         * If we encounter this, try to scroll the element with the expected contentDescription into
         * view and return that element
         * Look up the view hierarchy to find the closest ancestor AdapterView
         */
        private fun lookupOffscreenView(initialView: View, initialContentDescription: String): View {
            // Try scrolling the view with the expected content description into the viewport
            val dataInteraction = onData(
                    hasEntry(Matchers.equalTo("contentDescription"),
                            `is`(initialContentDescription))
            )

            // Look up the ancestry tree until we find an AdapterView
            var ancestorAdapter: ViewParent? = initialView.parent
            while (ancestorAdapter != null) {
                ancestorAdapter = ancestorAdapter.parent
                if (ancestorAdapter is AdapterView<*>) {
                    dataInteraction.inAdapterView(withView(ancestorAdapter))
                    break
                }
            }

            dataInteraction.check(matches(isDisplayed()))
            // If successful, use that view instead of the cached view
            return ViewGetter().getView(onView(withContentDescription(initialContentDescription)))
        }

        @Throws(NoSuchElementException::class, StaleElementException::class)
        fun getViewById(elementId: String?, checkStaleness: Boolean = true): View {
            elementId ?: throw InvalidArgumentException("Cannot find 'null' element")
            if (!ViewsCache.has(elementId)) {
                throw NoSuchElementException("The element identified by '$elementId' does not exist in the cache " +
                        "or has expired. Try to find it again")
            }

            val (resultView, initialContentDescription1) = Objects.requireNonNull<ViewState>(ViewsCache.get(elementId))

            // If the cached view is gone, throw stale element exception
            if (!resultView.isShown) {
                if (checkStaleness) {
                    throw StaleElementException(elementId)
                } else {
                    return resultView
                }
            }

            val initialContentDescription = charSequenceToNullableString(initialContentDescription1)
                    ?: return resultView
            val currentContentDescription = charSequenceToNullableString(resultView.contentDescription)
            if (currentContentDescription == initialContentDescription) {
                return resultView
            }

            try {
                ViewsCache.put(elementId, lookupOffscreenView(resultView, initialContentDescription))
            } catch (e: Exception) {
                if (e is EspressoException) {
                    if (!checkStaleness) {
                        return resultView
                    }
                    throw StaleElementException(elementId)
                }
                throw e
            }

            return resultView
        }
    }
}
