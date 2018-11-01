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

package io.appium.espressoserver.lib.model;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.EspressoException;
import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;

import org.hamcrest.Matchers;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.viewaction.ViewGetter;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static io.appium.espressoserver.lib.viewmatcher.WithView.withView;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;


@SuppressWarnings("unused")
public class Element {
    private final String ELEMENT;
    private final static Map<String, View> cache = new ConcurrentHashMap<>();
    private final static Map<String, String> contentDescriptionCache = new ConcurrentHashMap<>();

    public Element (View view) {
        ELEMENT = UUID.randomUUID().toString();
        cache.put(ELEMENT, view);

        // Cache the content description as well
        CharSequence contentDesc = view.getContentDescription();

        if (contentDesc != null) {
            contentDescriptionCache.put(ELEMENT, contentDesc.toString());
        }
    }

    public String getElementId() {
        return ELEMENT;
    }

    /**
     * Retrieve cached view and return the ViewInteraction
     * @param elementId
     * @return
     * @throws NoSuchElementException
     * @throws StaleElementException
     */
    public static ViewInteraction getViewInteractionById(String elementId) throws AppiumException {
        if (!exists(elementId)) {
            throw new NoSuchElementException(String.format("Invalid element ID %s", elementId));
        }
        View view = Element.getViewById(elementId);
        return onView(withView(view));
    }

    /**
     * Return the cached element
     * @param elementId
     * @return
     */
    public static View getViewById(String elementId) throws AppiumException {
        View view = getCachedView(elementId);

        if (!view.isShown()) {
            throw new StaleElementException(elementId);
        }
        return view;
    }

    public static boolean exists(String elementId) {
        return cache.containsKey(elementId);
    }

    public static View getCachedView(String elementId) throws NoSuchElementException, StaleElementException {
        if (!exists(elementId)) {
            throw new NoSuchElementException(String.format("No such element with ID %s", elementId));
        }

        View view = cache.get(elementId);

        if (!view.isShown()) {
            throw new StaleElementException(elementId);
        }

        // This is a special case:
        //
        // If the contentDescription of a cached view changed, it almost certainly means the rendering of an
        // AdapterView (ListView, GridView, Scroll, etc...) changed and the View contents were shuffled around.
        //
        // If we encounter this, try to scroll the element with the expected contentDescription into
        // view and return that element
        if (contentDescriptionCache.containsKey(elementId)) {
            String expectedContentDesc = contentDescriptionCache.get(elementId);

            if (!Objects.equals(view.getContentDescription(), expectedContentDesc)) {

                // Look up the view hierarchy to find the closest ancestor AdapterView
                ViewParent ancestorAdapter = view.getParent();
                while (ancestorAdapter != null && !(ancestorAdapter instanceof AdapterView)) {
                    ancestorAdapter = ancestorAdapter.getParent();
                }

                try {
                    // Try scrolling the view with the expected content description into the viewport
                    DataInteraction dataInteraction = onData(
                            hasEntry(Matchers.equalTo("contentDescription"), is(expectedContentDesc))
                    );
                    if (ancestorAdapter != null) {
                        dataInteraction.inAdapterView(withView((AdapterView) ancestorAdapter));
                    }
                    dataInteraction.check(matches(isDisplayed()));

                    // If successful, use that view instead of the cached view
                    view = (new ViewGetter()).getView(onView(withContentDescription(expectedContentDesc)));
                    cache.put(elementId, view);
                } catch (Exception e) {
                    if (e instanceof EspressoException) {
                        throw new StaleElementException(elementId);
                    }
                    throw e;
                }
            }
        }

        return view;
    }
}
