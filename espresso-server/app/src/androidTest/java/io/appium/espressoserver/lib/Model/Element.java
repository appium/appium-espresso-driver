package io.appium.espressoserver.lib.Model;

import android.support.test.espresso.ViewInteraction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Element {
    private String id;
    private static Map<String, ViewInteraction> cache = new HashMap<String, ViewInteraction>();

    public Element (ViewInteraction interaction) {
        id = UUID.randomUUID().toString();
        cache.put(id, interaction);
    }

    public String getId() {
        return id;
    }

    public static Map<String, ViewInteraction> getCache() {
        return cache;
    }
}
