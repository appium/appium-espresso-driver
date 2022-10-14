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

import android.view.View
import android.widget.AdapterView

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

import java.util.ArrayList

import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException
import io.appium.espressoserver.lib.model.Strategy
import io.appium.espressoserver.lib.viewaction.ViewGetter

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.AppNotIdleException
import androidx.test.espresso.DataInteraction
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.EspressoException
import androidx.test.espresso.PerformException
import androidx.test.espresso.Root
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.model.toJsonMatcher
import io.appium.espressoserver.lib.viewmatcher.withView
import io.appium.espressoserver.lib.viewmatcher.withXPath
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
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
    fun findBy(root: View?, strategy: Strategy, selector: String): ViewState? {
        val viewStates = findAllBy(root, strategy, selector, true)
        return if (viewStates.isEmpty()) null else viewStates[0]
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
    fun findAllBy(root: View?, strategy: Strategy, selector: String): List<ViewState> {
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
        return if (views.isEmpty()) null else views[0]
    }

    ///Find By different strategies
    private fun findAllBy(
        root: View?,
        strategy: Strategy,
        selector: String,
        findOne: Boolean
    ): List<ViewState> {
        when (strategy) {
            Strategy.ID -> {
                // with ID
                // find id from target context
                val context = getApplicationContext<Context>()
                var patchedSelector = selector
                if (!selector.matches(ID_PATTERN.toRegex())) {
                    patchedSelector = "${context.packageName}:id/$selector"
                    AndroidLogger.info("Rewrote Id selector to '$patchedSelector'")
                }
                var id = context.resources.getIdentifier(patchedSelector, "Id", context.packageName)
                if (id == 0 && patchedSelector != selector) {
                    id = context.resources.getIdentifier(selector, "Id", context.packageName)
                }

                return if (id == 0) emptyList() else getViews(root, withId(id), findOne).map { ViewState(it) }
            }
            Strategy.CLASS_NAME -> {
                // with class name
                val cls = try {
                    Class.forName(selector)
                } catch (e: ClassNotFoundException) {
                    return emptyList()
                }
                return getViews(root, instanceOf(cls), findOne).map { ViewState(it) }
            }
            Strategy.TEXT ->
                // with text
                return getViews(root, withText(selector), findOne).map { ViewState(it) }
            Strategy.ACCESSIBILITY_ID -> {
                val result = getViews(root, withContentDescription(selector), findOne)

                // If the item is not found on the screen, use 'onData' to try
                // to scroll it into view and then locate it again
                return if (result.isEmpty() && canScrollToViewWithContentDescription(root, selector))
                    getViews(root, withContentDescription(selector), findOne).map { ViewState(it) }
                else
                    result.map { ViewState(it) }
            }
            Strategy.XPATH ->
                // If we're only looking for one item that matches xpath, pass it index 0 or else
                // Espresso throws an AmbiguousMatcherException
                return if (findOne) {
                    getViews(root, withXPath(root, selector, 0), true).map { ViewState(it) }
                } else {
                    getViews(root, withXPath(root, selector), false).map { ViewState(it) }
                }
            Strategy.VIEW_TAG ->
                return getViews(
                    root,
                    withTagValue(allOf(instanceOf(String::class.java), equalTo(selector as Any))),
                    findOne
                ).map { ViewState(it) }
            Strategy.DATAMATCHER -> {
                val matcher = selector.toJsonMatcher()
                return try {
                    getViewsFromDataInteraction(root, onData(matcher.query.matcher))
                        .map { ViewState(it) }
                } catch (e: PerformException) {
                    // Perform Exception means nothing was found. Return empty list
                    emptyList()
                }
            }
            Strategy.VIEWMATCHER -> {
                val matcherJson = selector.toJsonMatcher()
                return try {
                    @Suppress("UNCHECKED_CAST")
                    getViews(
                        root,
                        matcherJson.query.matcher as Matcher<View>,
                        findOne,
                        matcherJson.query.scope as Matcher<Root>?
                    ).map { ViewState(it, rootMatcher = matcherJson.query.scope) }
                } catch (e: PerformException) {
                    // Perform Exception means nothing was found. Return empty list
                    emptyList()
                }
            }
            else -> throw InvalidSelectorException("Strategy is not implemented: ${strategy.strategyName}")
        }
    }

    /**
     * Attempts to scroll to a view with the content description using onData
     *
     * @param contentDesc Content description
     * @return
     */
    private fun canScrollToViewWithContentDescription(parentView: View?, contentDesc: String): Boolean {
        try {
            val dataInteraction = onData(
                    hasEntry(equalTo("contentDescription"), `is`(contentDesc))
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

    private fun getViews(
            root: View?,
            matcher: Matcher<View>,
            findOne: Boolean,
            rootMatcher: Matcher<Root>? = null
    ): List<View> {
        // If it's just one view we want, return a singleton list
        if (findOne) {
            try {
                val viewInteraction = if (root == null) {
                    if (rootMatcher == null) {
                        onView(withIndex(matcher, 0))
                    } else {
                        onView(withIndex(matcher, 0)).inRoot(rootMatcher)
                    }
                } else {
                    if (rootMatcher == null) {
                        onView(allOf(isDescendantOfA(`is`(root)), withIndex(matcher, 0)))
                    } else {
                        onView(allOf(isDescendantOfA(`is`(root)), withIndex(matcher, 0))).inRoot(rootMatcher)
                    }
                }
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
                val viewInteraction = if (root == null) {
                    if (rootMatcher == null) {
                        onView(withIndex(matcher, i++))
                    } else {
                        onView(withIndex(matcher, i++)).inRoot(rootMatcher)
                    }
                } else {
                    if (rootMatcher == null) {
                        onView(allOf(isDescendantOfA(`is`(root)), withIndex(matcher, i++)))
                    } else {
                        onView(allOf(isDescendantOfA(`is`(root)), withIndex(matcher, i++))).inRoot(rootMatcher)
                    }
                }
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
