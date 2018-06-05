package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

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

    @Nullable
    public InputSource getInputSource(InputSource inputSource) {
        return inputSources.get(inputSource.getId());
    }

    @Nullable
    public InputSource getInputSource(String id) {
        return inputSources.get(id);
    }

    public boolean hasInputSource(String id) {
        return inputSources.containsKey(id);
    }

    /**
     * There is supposed to be on ActiveInputSource per session
     *
     * Since Espresso is one session per device return a global session
     *
     * If need be though we could amend it in the future to overload this method
     * and get an instance by the sessionId
     * @return Global instance of ActiveInputSources
     */
    public synchronized static ActiveInputSources getInstance() {
        if (globalActiveInputSources == null) {
            globalActiveInputSources = new ActiveInputSources();
        }
        return globalActiveInputSources;
    }
}
