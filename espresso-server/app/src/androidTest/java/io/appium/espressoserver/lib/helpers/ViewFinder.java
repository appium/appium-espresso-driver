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

package io.appium.espressoserver.lib.helpers;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException;
import io.appium.espressoserver.lib.model.Strategy;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static io.appium.espressoserver.lib.viewmatcher.WithXPath.withXPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.endsWith;

/**
 * Helper methods to find elements based on locator strategies and selectors
 */
public class ViewFinder {

    @Nullable
    public static ViewInteraction findBy(Strategy strategy, String selector)
            throws InvalidStrategyException, XPathLookupException {
        return findBy(null, strategy, selector);
    }

    /**
     * Find one instance of an element that matches the locator criteria
     *
     * @param root     Parent view instance or null if the search is executed against the
     *                 full hierarchy
     * @param strategy Locator strategy (xpath, class name, etc...)
     * @param selector Selector string
     * @return
     * @throws InvalidStrategyException
     * @throws XPathLookupException
     */
    @Nullable
    public static ViewInteraction findBy(
            @Nullable View root, Strategy strategy, String selector)
            throws InvalidStrategyException, XPathLookupException {
        List<ViewInteraction> viewInteractions = findAllBy(root, strategy, selector, true);
        if (viewInteractions.isEmpty()) {
            return null;
        }
        return viewInteractions.get(0);
    }

    @Nullable
    public static List<ViewInteraction> findAllBy(Strategy strategy, String selector)
            throws InvalidStrategyException, XPathLookupException {
        return findAllBy(null, strategy, selector, false);
    }

    /**
     * Find all instances of an element that matches the locator criteria
     *
     * @param root     Parent view instance or null if the search is executed against the
     *                 full hierarchy
     * @param strategy Locator strategy (xpath, class name, etc...)
     * @param selector Selector string
     * @return
     * @throws InvalidStrategyException
     * @throws XPathLookupException
     */
    public static List<ViewInteraction> findAllBy(@Nullable View root,
                                                  Strategy strategy, String selector)
            throws InvalidStrategyException, XPathLookupException {
        return findAllBy(root, strategy, selector, false);
    }

    /**
     * Returns the currently focused element or null if
     * there is no such element
     *
     * @return focused element instance or null
     */
    @Nullable
    public static ViewInteraction findActive() {
        List<ViewInteraction> viewInteractions = getViewInteractions(null,
                new TypeSafeMatcher<View>() {
                    @Override
                    protected boolean matchesSafely(View item) {
                        return item.isFocused();
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("is focused");
                    }
                },
                true);
        if (viewInteractions.isEmpty()) {
            return null;
        }
        return viewInteractions.get(0);
    }

    ///Find By different strategies
    private static List<ViewInteraction> findAllBy(@Nullable View root,
                                                   Strategy strategy, String selector, boolean findOne)
            throws InvalidStrategyException, XPathLookupException {
        List<ViewInteraction> matcher;

        switch (strategy) {
            case ID: // with ID

                // find id from target context
                Context context = InstrumentationRegistry.getTargetContext();
                int id = context.getResources().getIdentifier(selector, "Id",
                        InstrumentationRegistry.getTargetContext().getPackageName());

                matcher = getViewInteractions(root, withId(id), findOne);
                break;
            case CLASS_NAME:
                // with class name
                // TODO: improve this finder with instanceOf
                matcher = getViewInteractions(root, withClassName(endsWith(selector)), findOne);
                break;
            case TEXT:
                // with text
                matcher = getViewInteractions(root, withText(selector), findOne);
                break;
            case ACCESSIBILITY_ID:
                // with content description
                matcher = getViewInteractions(root, withContentDescription(selector), findOne);
                break;
            case XPATH:
                // If we're only looking for one item that matches xpath, pass it index 0 or else
                // Espresso throws an AmbiguousMatcherException
                if (findOne) {
                    matcher = getViewInteractions(root, withXPath(selector, 0), true);
                } else {
                    matcher = getViewInteractions(root, withXPath(selector), false);
                }
                break;
            default:
                throw new InvalidStrategyException(String.format("Strategy is not implemented: %s", strategy.getStrategyName()));
        }

        return matcher;
    }

    private static List<ViewInteraction> getViewInteractions(
            @Nullable View root, Matcher<View> matcher, boolean findOne) {
        // If it's just one view we want, return a singleton list
        if (findOne) {
            try {
                if (root == null) {
                    return Collections.singletonList(onView(withIndex(matcher, 0)));
                }
                return Collections.singletonList(
                        onView(withIndex(matcher, 0)).inRoot(withDecorView(is(root)))
                );
            } catch (NoMatchingViewException e) {
                return Collections.emptyList();
            }
        }

        // If we want all views that match the criteria, start looking for ViewInteractions by
        // index and add each match to the List. As soon as we find no match, break the loop
        // and return the list
        List<ViewInteraction> viewInteractions = new ArrayList<>();
        int i = 0;
        do {
            try {
                ViewInteraction viewInteraction;
                if (root == null) {
                    viewInteraction = onView(withIndex(matcher, i++));
                } else {
                    viewInteraction = onView(withIndex(matcher, i++))
                            .inRoot(withDecorView(is(root)));
                }
                viewInteractions.add(viewInteraction);
            } catch (NoMatchingViewException e) {
                return viewInteractions;
            }
        } while (i < Integer.MAX_VALUE);
        return viewInteractions;
    }

    private static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}
