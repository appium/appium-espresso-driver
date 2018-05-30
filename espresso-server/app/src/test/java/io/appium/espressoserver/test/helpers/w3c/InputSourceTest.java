package io.appium.espressoserver.test.helpers.w3c;

import org.junit.Before;
import org.junit.Test;

import io.appium.espressoserver.lib.helpers.w3c.models.inputsources.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.inputsources.KeyInputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.inputsources.PointerInputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.inputsources.PointerTypeEnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class InputSourceTest {

    @Before
    public void before() {
    }

    @Test
    public void shouldDeserializeInputSource() {
        String postJson = "{\"id\":\"something\"}";
        InputSource inputSource = InputSource.deserialize(postJson);
        assertEquals(inputSource.getId(), "something");
    }

    @Test
    public void shouldDeserializePointerInputSource() {
        String postJson = "{\"type\":\"pointer\",\"id\":\"something\", \"parameters\": {\"pointerType\": \"touch\"}}";
        PointerInputSource inputSource = (PointerInputSource) InputSource.deserialize(postJson);
        assertEquals(inputSource.getId(), "something");
        assertEquals(inputSource.getParameters().getPointerType(), PointerTypeEnum.TOUCH);
    }

    @Test
    public void shouldDeserializeKeyInputSource() {
        String postJson = "{\"type\":\"key\",\"id\":\"something2\"}";
        KeyInputSource inputSource = (KeyInputSource) InputSource.deserialize(postJson);
        assertEquals(inputSource.getId(), "something2");
    }

    public void shouldDeserializePauseActions() {
        String postJson = "{\"id\":\"finger2\",\"actions\":[{\"type\":\"pause\",\"duration\":500}]}";
        InputSource inputSource = InputSource.deserialize(postJson);
    }
}