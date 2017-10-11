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

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

@SuppressWarnings("unused")
public class Element {
    private final String ELEMENT;
    private final static Map<String, ViewInteraction> cache = new ConcurrentHashMap<>();

    public Element (ViewInteraction interaction) {
        ELEMENT = UUID.randomUUID().toString();
        cache.put(ELEMENT, interaction);
    }

    public String getElementId() {
        return ELEMENT;
    }

    public static ViewInteraction getById(String elementId) throws NoSuchElementException, StaleElementException {
        if (!exists(elementId)) {
            throw new NoSuchElementException(String.format("Invalid element ID %s", elementId));
        }

        ViewInteraction viewInteraction = cache.get(elementId);

        // Check if the element is stale
        try {
            viewInteraction.check(matches(isDisplayed()));
        } catch (NoMatchingViewException nme) {
            throw new StaleElementException(elementId);
        }

        return viewInteraction;
    }

    public static boolean exists(String elementId) {
        return cache.containsKey(elementId);
    }
}
