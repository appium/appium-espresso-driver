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
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.EspressoException
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.google.gson.annotations.SerializedName
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException
import io.appium.espressoserver.lib.helpers.ComposeViewCache
import io.appium.espressoserver.lib.helpers.StringHelpers.charSequenceToNullableString
import io.appium.espressoserver.lib.helpers.ViewState
import io.appium.espressoserver.lib.helpers.EspressoViewsCache
import io.appium.espressoserver.lib.viewaction.ViewGetter
import io.appium.espressoserver.lib.viewmatcher.withView
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasEntry
import java.util.*

const val W3C_ELEMENT_KEY = "element-6066-11e4-a52e-4f735466cecf"
const val JSONWP_ELEMENT_KEY = "ELEMENT"

interface BaseElement {
    val element: String
}

class EspressoElement(viewState: ViewState) : BaseElement {
    @Suppress("JoinDeclarationAndAssignment")
    @SerializedName(JSONWP_ELEMENT_KEY, alternate = [W3C_ELEMENT_KEY])
    override val element: String

    init {
        element = UUID.randomUUID().toString()
        EspressoViewsCache.put(element, viewState)
    }

    companion object {
        /**
         * Retrieve cached view and return the ViewInteraction
         */
        fun getViewInteractionById(elementId: String?): ViewInteraction {
            val cachedView = getCachedViewStateById(elementId)
            return if (cachedView.rootMatcher == null)
                onView(withView(cachedView.view))
            else
                onView(withView(cachedView.view)).inRoot(cachedView.rootMatcher)
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
        private fun lookupOffscreenView(
            initialView: View,
            initialContentDescription: String
        ): View {
            // Try scrolling the view with the expected content description into the viewport
            val dataInteraction = onData(
                hasEntry(
                    Matchers.equalTo("contentDescription"),
                    `is`(initialContentDescription)
                )
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
        fun getCachedViewStateById(elementId: String?, checkStaleness: Boolean = true): ViewState {
            elementId ?: throw InvalidArgumentException("Cannot find 'null' element")
            if (!EspressoViewsCache.has(elementId)) {
                throw NoSuchElementException(
                    "The element identified by '$elementId' does not exist in the cache " +
                            "or has expired. Try to find it again"
                )
            }

            val resultState = Objects.requireNonNull<ViewState>(
                EspressoViewsCache.get(elementId)
            )

            // If the cached view is gone, throw stale element exception
            if (!resultState.view.isShown) {
                if (checkStaleness) {
                    throw StaleElementException(elementId)
                } else {
                    return resultState
                }
            }

            val initialContentDescription =
                charSequenceToNullableString(resultState.initialContentDescription)
                    ?: return resultState
            val currentContentDescription =
                charSequenceToNullableString(resultState.view.contentDescription)
            if (currentContentDescription == initialContentDescription) {
                return resultState
            }

            try {
                EspressoViewsCache.put(
                    elementId,
                    ViewState(
                        lookupOffscreenView(resultState.view, initialContentDescription),
                        rootMatcher = resultState.rootMatcher
                    )
                )
            } catch (e: Exception) {
                if (e is EspressoException) {
                    if (!checkStaleness) {
                        return resultState
                    }
                    throw StaleElementException(elementId)
                }
                throw e
            }

            return resultState
        }
    }
}

class ComposeElement(node: SemanticsNodeInteraction) : BaseElement {
    @Suppress("JoinDeclarationAndAssignment")
    @SerializedName(JSONWP_ELEMENT_KEY, alternate = [W3C_ELEMENT_KEY])
    override val element: String

    init {
        element = UUID.randomUUID().toString()
        ComposeViewCache.put(element, node)
    }
}