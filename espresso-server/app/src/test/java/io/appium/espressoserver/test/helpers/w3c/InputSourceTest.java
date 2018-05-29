package io.appium.espressoserver.test.helpers.w3c;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.handlers.RequestHandler;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.PointerInputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.PointerTypeEnum;
import io.appium.espressoserver.lib.http.RouteDefinition;
import io.appium.espressoserver.lib.model.AppiumParams;

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
        String postJson = "{\"type\":\"pointer\",\"id\":\"something\", \"pointerType\": \"mouse\"}";
        PointerInputSource inputSource = (PointerInputSource) InputSource.deserialize(postJson);
        assertEquals(inputSource.getId(), "something");
        assertEquals(inputSource.getPointerTypeEnum(), PointerTypeEnum.MOUSE);
    }

    @Test
    public void shouldDeserializeKeyInputSource() {
        String postJson = "{\"type\":\"pointer\",\"id\":\"something\", \"pointerType\": \"mouse\"}";
        PointerInputSource inputSource = (PointerInputSource) InputSource.deserialize(postJson);
        assertEquals(inputSource.getId(), "something");
        assertEquals(inputSource.getPointerTypeEnum(), PointerTypeEnum.MOUSE);
    }
}