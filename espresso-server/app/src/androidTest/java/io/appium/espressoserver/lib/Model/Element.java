package io.appium.espressoserver.lib.Model;

import android.support.test.espresso.ViewInteraction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class Element {
    private final String ELEMENT;
    private final static Map<String, ViewInteraction> cache = new HashMap<>();

    public Element (ViewInteraction interaction) {
        ELEMENT = UUID.randomUUID().toString();
        cache.put(ELEMENT, interaction);
    }

    public String getElementId() {
        return ELEMENT;
    }

    public static ViewInteraction getById(String elementId) {
        return cache.get(elementId);
    }

    public static boolean exists(String elementId) {
        return cache.containsKey(elementId);
    }
}
