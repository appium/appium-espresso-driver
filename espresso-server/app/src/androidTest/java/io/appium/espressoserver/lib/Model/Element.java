package io.appium.espressoserver.lib.Model;

import android.support.test.espresso.ViewInteraction;

import java.util.HashMap;
import java.util.Map;

public class Element {
    private int id;
    private static int autoId = 0;
    private static Map<Integer, ViewInteraction> cache = new HashMap<Integer, ViewInteraction>();

    public Element (ViewInteraction interaction) {
        id = autoId++; // TODO: Make this a UUID
        cache.put(id, interaction);
    }

    public int getId() {
        return id;
    }

    public static Map<Integer, ViewInteraction> getCache() {
        return cache;
    }
}
