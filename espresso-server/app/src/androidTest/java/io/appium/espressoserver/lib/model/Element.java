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

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;

import static android.support.test.espresso.Espresso.onView;
import static io.appium.espressoserver.lib.viewmatcher.WithView.withView;


@SuppressWarnings("unused")
public class Element {
    private final String ELEMENT;
    private final static Map<String, View> cache = new ConcurrentHashMap<>();

    public Element (View view) {
        ELEMENT = UUID.randomUUID().toString();
        cache.put(ELEMENT, view);
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
    public static View getViewById(final String elementId) throws AppiumException {
        UiControllerRunnable<View> runnable = new UiControllerRunnable<View>() {
            @Override
            public View run(UiController uiController) throws NoSuchElementException, StaleElementException {
                if (!exists(elementId)) {
                    throw new NoSuchElementException(String.format("Invalid element ID %s", elementId));
                }

                // Don't try accessing the View until the main thread is idle
                uiController.loopMainThreadUntilIdle();

                // Get the cached view
                View view = cache.get(elementId);
                if (!view.isShown()) {
                    throw new StaleElementException(elementId);
                }

                return view;
            }
        };

        return new UiControllerPerformer<>(runnable).run();
    }

    public static View getViewById(String elementId, boolean skipWaitForMainThreadIdle) throws AppiumException {
        if (skipWaitForMainThreadIdle) {
            if (!exists(elementId)) {
                throw new NoSuchElementException(String.format("Invalid element ID %s", elementId));
            }
            View view = cache.get(elementId);

            if (!view.isShown()) {
                throw new StaleElementException(elementId);
            }
            return view;
        }
        return getViewById(elementId);
    }

    public static boolean exists(String elementId) {
        return cache.containsKey(elementId);
    }
}
