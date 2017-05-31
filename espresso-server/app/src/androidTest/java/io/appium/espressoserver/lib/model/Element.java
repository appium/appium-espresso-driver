package io.appium.espressoserver.lib.model;

import android.support.test.espresso.ViewInteraction;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    public static ViewInteraction getById(String elementId) throws NoSuchElementException {
        if (!exists(elementId)) {
            throw new NoSuchElementException(String.format("Invalid element ID %s", elementId));
        }
        return cache.get(elementId);
    }

    public static boolean exists(String elementId) {
        return cache.containsKey(elementId);
    }
}
