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

import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;

import org.hamcrest.Matchers;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.EspressoException;
import androidx.test.espresso.ViewInteraction;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.helpers.ViewState;
import io.appium.espressoserver.lib.helpers.ViewsCache;
import io.appium.espressoserver.lib.viewaction.ViewGetter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static io.appium.espressoserver.lib.viewmatcher.WithView.withView;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;


@SuppressWarnings("unused")
public class Element {
    private final String ELEMENT;
    private final static ViewsCache cache = ViewsCache.INSTANCE;

    public Element(View view) {
        String id = cache.getViewId(view);
        if (id == null) {
            ELEMENT = UUID.randomUUID().toString();
            cache.put(ELEMENT, view);
        } else {
            ELEMENT = id;
        }
    }

    public String getElementId() {
        return ELEMENT;
    }

    /**
     * Retrieve cached view and return the ViewInteraction
     *
     * @param elementId
     * @return
     * @throws NoSuchElementException
     * @throws StaleElementException
     */
    public static ViewInteraction getViewInteractionById(String elementId) throws AppiumException {
        View view = Element.getViewById(elementId);
        return onView(withView(view));
    }

    public static View getViewById(String elementId) throws NoSuchElementException, StaleElementException {
        if (!cache.has(elementId)) {
            throw new NoSuchElementException(String.format("No such element with ID %s", elementId));
        }

        ViewState viewState = cache.get(elementId);

        if (viewState instanceof ViewState.DETACHED) {
            throw new StaleElementException(elementId);
        }

        //noinspection ConstantConditions
        View resultView = ((ViewState.ATTACHED) viewState).getView();
        String initialContentDescription = null;
        if (((ViewState.ATTACHED) viewState).getInitialContentDescription() != null) {
            //noinspection ConstantConditions
            initialContentDescription = ((ViewState.ATTACHED) viewState).getInitialContentDescription().toString();
        }

        // This is a special case:
        //
        // If the contentDescription of a cached view changed, it almost certainly means the rendering of an
        // AdapterView (ListView, GridView, Scroll, etc...) changed and the View contents were shuffled around.
        //
        // If we encounter this, try to scroll the element with the expected contentDescription into
        // view and return that element
        if (!Objects.equals(resultView.getContentDescription(), initialContentDescription)) {

            // Look up the view hierarchy to find the closest ancestor AdapterView
            ViewParent ancestorAdapter = resultView.getParent();
            while (ancestorAdapter != null && !(ancestorAdapter instanceof AdapterView)) {
                ancestorAdapter = ancestorAdapter.getParent();
            }

            try {
                // Try scrolling the view with the expected content description into the viewport
                DataInteraction dataInteraction = onData(
                        hasEntry(Matchers.equalTo("contentDescription"), is(resultView.getContentDescription()))
                );
                if (ancestorAdapter != null) {
                    dataInteraction.inAdapterView(withView((AdapterView) ancestorAdapter));
                }
                dataInteraction.check(matches(isDisplayed()));

                // If successful, use that view instead of the cached view
                resultView = (new ViewGetter()).getView(onView(withContentDescription(resultView.getContentDescription().toString())));
                cache.put(elementId, resultView);
            } catch (Exception e) {
                if (e instanceof EspressoException) {
                    throw new StaleElementException(elementId);
                }
                throw e;
            }

        }
        //noinspection ConstantConditions
        return resultView;
    }
}
