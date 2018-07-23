package io.appium.espressoserver.lib.helpers.w3c.state;

import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;

/**
 * Active Input Source defined in W3C spec
 *
 * (see https://www.w3.org/TR/webdriver/#terminology-0)
 */
public class ActiveInputSources {
    private final Map<String, InputSource> inputSources = new WeakHashMap<>();
    private static final Map<String, ActiveInputSources> activeInputSources = new WeakHashMap<>();

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
     * Get the `active input sources` table for a session
     *
     * @return Global instance of ActiveInputSources
     */
    public synchronized static ActiveInputSources getActiveInputSourcesForSession(String sessionId) {
        ActiveInputSources globalInputStateTable = activeInputSources.get(sessionId);
        if (globalInputStateTable == null) {
            activeInputSources.put(sessionId, new ActiveInputSources());
            globalInputStateTable = activeInputSources.get(sessionId);
        }
        return globalInputStateTable;
    }
}
