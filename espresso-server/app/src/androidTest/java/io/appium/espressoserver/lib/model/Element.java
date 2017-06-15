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
