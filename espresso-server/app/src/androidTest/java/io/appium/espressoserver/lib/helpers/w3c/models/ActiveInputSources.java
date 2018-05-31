package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.HashMap;
import java.util.Map;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;

/**
 * Active Input Source defined in W3C spec
 *
 * (see https://www.w3.org/TR/webdriver/#terminology-0)
 */
public class ActiveInputSources {
    private Map<String, InputSource> inputSources = new HashMap<>();
    private static ActiveInputSources globalActiveInputSources;

    public void addInputSource(InputSource inputSource) throws InvalidArgumentException {
        inputSources.put(inputSource.getId(), inputSource);
    }

    /**
     * Remove an input source and also remove it from InputStateTable
     * @param inputSource Source to remove
     */
    public void removeInputSource(InputSource inputSource) {
        removeInputSource(inputSource.getId());
    }

    public void removeInputSource(String id) {
        inputSources.remove(id);
    }

    public InputSource getInputSource(InputSource inputSource) {
        return inputSources.get(inputSource.getId());
    }

    /**
     * There is supposed to be on ActiveInputSource per session
     *
     * Since Espresso is one session per device return a global session
     *
     * If need be though we could amend it in the future to overload this method
     * and get an instance by the sessionId
     * @return
     */
    public static ActiveInputSources getInstance() {
        if (globalActiveInputSources == null) {
            globalActiveInputSources = new ActiveInputSources();
        }
        return globalActiveInputSources;
    }
}
