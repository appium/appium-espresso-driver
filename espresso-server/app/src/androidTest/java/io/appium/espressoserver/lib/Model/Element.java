package io.appium.espressoserver.lib.Model;

import android.support.test.espresso.ViewInteraction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Element extends Appium {
    private String ELEMENT;
    private static Map<String, ViewInteraction> cache = new HashMap<String, ViewInteraction>();

    public Element (ViewInteraction interaction) {
        ELEMENT = UUID.randomUUID().toString();
        cache.put(ELEMENT, interaction);
    }

    public String getElementId() {
        return ELEMENT;
    }

    public static Map<String, ViewInteraction> getCache() {
        return cache;
    }
}
