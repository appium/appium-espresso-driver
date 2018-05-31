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

    public void addInputSource(InputSource inputSource) throws InvalidArgumentException {
        if (inputSource.getId() == null) {
            throw new InvalidArgumentException("Input source is missing ID");
        }
        this.inputSources.put(inputSource.getId(), inputSource);
    }

    /**
     * Remove an input source and also remove it from InputStateTable
     * @param inputSource Source to remove
     */
    public void removeInputSource(InputSource inputSource) {
        this.inputSources.remove(inputSource.getId());
    }

}
