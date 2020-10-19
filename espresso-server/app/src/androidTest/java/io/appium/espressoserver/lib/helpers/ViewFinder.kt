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

package io.appium.espressoserver.lib.helpers

import android.content.Context

import androidx.test.espresso.DataInteraction
import androidx.test.espresso.EspressoException
import androidx.test.espresso.PerformException
import android.view.View
import android.widget.AdapterView

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher

import java.util.ArrayList

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException
import io.appium.espressoserver.lib.model.Strategy
import io.appium.espressoserver.lib.viewaction.ViewGetter

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.AppNotIdleException
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.model.toJsonMatcher
import io.appium.espressoserver.lib.viewmatcher.withView
import io.appium.espressoserver.lib.viewmatcher.withXPath
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.instanceOf

/**
 * Helper methods to find elements based on locator strategies and selectors
 */
object ViewFinder {
    private const val ID_PATTERN = "[\\S]+:id/[\\S]+"
    private const val APP_NOT_IDLE_MESSAGE = "The application is expected to be in idle state in order for Espresso to interact with it. " +
            "Review the threads dump below to know more on which entity is hogging the events loop "

    /**
     * Find one instance of an element that matches the locator criteria
     *
     * @param root     Parent view instance or null if the search is executed against the
     * full hierarchy
     * @param strategy Locator strategy (xpath, class name, etc...)
     * @param selector Selector string
     * @return
     * @throws InvalidSelectorException
     * @throws XPathLookupException
     */
    @Throws(AppiumException::class)
    fun findBy(
            root: View?, strategy: Strategy, selector: String): View? {
        val viewInteractions = findAllBy(root, strategy, selector, true)
        return if (viewInteractions.isEmpty()) {
            null
        } else viewInteractions[0]
    }

    /**
     * Find all instances of an element that matches the locator criteria
     *
     * @param root     Parent view instance or null if the search is executed against the
     * full hierarchy
     * @param strategy Locator strategy (xpath, class name, etc...)
     * @param selector Selector string
     * @return
     * @throws InvalidSelectorException
     * @throws XPathLookupException
     */
    @Throws(AppiumException::class)
    fun findAllBy(root: View?,
                  strategy: Strategy, selector: String): List<View> {
        return findAllBy(root, strategy, selector, false)
    }

    /**
     * Returns the currently focused element or null if
     * there is no such element
     *
     * @return focused element instance or null
     */
    fun findActive(): View? {
        val views = getViews(null,
                object : TypeSafeMatcher<View>() {
                    override fun matchesSafely(item: View): Boolean {
                        return item.isFocused
                    }

                    override fun describeTo(description: Description) {
                        description.appendText("is focused")
                    }
                },
                true)
        return if (views.isEmpty()) {
            null
        } else views[0]
    }

    ///Find By different strategies
    @Throws(AppiumException::class)
    private fun findAllBy(root: View?, strategy: Strategy,
                          selector: String, findOne: Boolean): List<View> {
        @Suppress("NAME_SHADOWING") var selector = selector
        var views: List<View>
        when (strategy) {
            Strategy.ID // with ID
            -> {

                // find id from target context
                val context = getApplicationContext<Context>()
                if (!selector.matches(ID_PATTERN.toRegex())) {
                    selector = "${context.packageName}:id/$selector"
                    AndroidLogger.info("Rewrote Id selector to '$selector'")
                }
                val id = context.resources.getIdentifier(selector, "Id", context.packageName)

                views = getViews(root, withId(id), findOne)
            }
            Strategy.CLASS_NAME ->
                // with class name
                // TODO: improve this finder with instanceOf
                views = getViews(root, withClassName(endsWith(selector)), findOne)
            Strategy.TEXT ->
                // with text
                views = getViews(root, withText(selector), findOne)
            Strategy.ACCESSIBILITY_ID -> {
                views = getViews(root, withContentDescription(selector), findOne)

                // If the item is not found on the screen, use 'onData' to try
                // to scroll it into view and then locate it again
                if (views.isEmpty() && canScrollToViewWithContentDescription(root, selector)) {
                    views = getViews(root, withContentDescription(selector), findOne)
                }
            }
            Strategy.XPATH ->
                // If we're only looking for one item that matches xpath, pass it index 0 or else
                // Espresso throws an AmbiguousMatcherException
                views = if (findOne) {
                    getViews(root, withXPath(root, selector, 0), true)
                } else {
                    getViews(root, withXPath(root, selector), false)
                }
            Strategy.VIEW_TAG -> views = getViews(root, withTagValue(allOf(instanceOf(String::class.java),
                    equalTo(selector as Any))), findOne)
            Strategy.DATAMATCHER -> {
                val matcher = selector.toJsonMatcher()
                views = try {
                    getViewsFromDataInteraction(root, onData(matcher.matcher))
                } catch (e: PerformException) {
                    // Perform Exception means nothing was found. Return empty list
                    emptyList()
                }
            }
            Strategy.VIEWMATCHER -> {
                val matcherJson = selector.toJsonMatcher()
                views = try {
                    @Suppress("UNCHECKED_CAST")
                    getViewsFromViewMatcher(root, matcherJson.matcher as Matcher<View>)
                } catch (e: PerformException) {
                    // Perform Exception means nothing was found. Return empty list
                    emptyList()
                }
            }
            else -> throw InvalidSelectorException("Strategy is not implemented: ${strategy.strategyName}")
        }

        return views
    }

    /**
     * Attempts to scroll to a view with the content description using onData
     *
     * @param contentDesc Content description
     * @return
     */
    private fun canScrollToViewWithContentDescription(parentView: View?,
                                                      contentDesc: String): Boolean {
        try {
            val dataInteraction = onData(
                    hasEntry(Matchers.equalTo("contentDescription"), `is`(contentDesc))
            )

            // If the parentView provided is an AdapterView, set 'inAdapterView' so that the
            // selector is restricted to the adapter subtree
            if (parentView is AdapterView<*>) {
                dataInteraction.inAdapterView(withView(parentView))
            }
            dataInteraction.check(matches(isDisplayed()))
        } catch (e: Exception) {
            return false
        }

        return true
    }

    private fun getViewsFromDataInteraction(
            root: View?, dataInteraction: DataInteraction
    ): List<View> {
        // Defensive copy
        var dataInteractionCopy = dataInteraction

        // Look up the view hierarchy to find the closest ancestor AdapterView
        var ancestorAdapterView = root
        while (ancestorAdapterView != null && ancestorAdapterView !is AdapterView<*>) {
            val parent = ancestorAdapterView.parent
            ancestorAdapterView = parent as? View
        }
        ancestorAdapterView?.let {
            dataInteractionCopy = dataInteractionCopy.inAdapterView(withView(it))
        }

        return listOf(ViewGetter().getView(dataInteractionCopy))
    }

    private fun getViewsFromViewMatcher(root: View?, matcher: Matcher<View>): List<View> {
        val viewInteraction = if (root == null)
            onView(matcher)
        else
            onView(allOf(isDescendantOfA(`is`(root)), matcher))
        return listOf(ViewGetter().getView(viewInteraction))
    }

    private fun getViews(
            root: View?, matcher: Matcher<View>, findOne: Boolean): List<View> {
        // If it's just one view we want, return a singleton list
        if (findOne) {
            try {
                val viewInteraction = if (root == null)
                    onView(withIndex(matcher, 0))
                else
                    onView(allOf(isDescendantOfA(`is`(root)), withIndex(matcher, 0)))
                return listOf(ViewGetter().getView(viewInteraction))
            } catch (e: AppNotIdleException){
                throw InvalidElementStateException(APP_NOT_IDLE_MESSAGE + getThreadDump(), e)
            } catch (e: Exception) {
                if (e is EspressoException) {
                    return emptyList()
                }
                throw e
            }
        }

        // If we want all views that match the criteria, start looking for ViewInteractions by
        // index and add each match to the List. As soon as we find no match, break the loop
        // and return the list
        val viewInteractions = ArrayList<View>()
        var i = 0
        do {
            try {
                val viewInteraction = if (root == null)
                    onView(withIndex(matcher, i++))
                else
                    onView(allOf(isDescendantOfA(`is`(root)), withIndex(matcher, i++)))
                val view = ViewGetter().getView(viewInteraction)
                viewInteractions.add(view)
            } catch (e: AppNotIdleException){
                throw InvalidElementStateException(APP_NOT_IDLE_MESSAGE + getThreadDump(), e)
            } catch (e: Exception) {
                if (e is EspressoException) {
                    return viewInteractions
                }
                throw e
            }

        } while (i < Integer.MAX_VALUE)
        return viewInteractions
    }

    private fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var currentIndex = 0

            override fun describeTo(description: Description) {
                description.appendText("with index: ")
                description.appendValue(index)
                matcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                return matcher.matches(view) && currentIndex++ == index
            }
        }
    }
}
