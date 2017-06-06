package io.appium.espressoserver.test.http;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.handlers.RequestHandler;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.http.RouteDefinition;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class RouteDefinitionTest {
    private final RequestHandler<AppiumParams, Void> dummyHandler = new RequestHandler<AppiumParams, Void>() {
        @Override
        public Void handle(AppiumParams params) throws AppiumException {
            return null;
        }
    };

    private RouteDefinition routeDefinitionWithOneParam;
    private RouteDefinition routeDefinitionWithZeroParams;
    private RouteDefinition routeDefinitionWithTwoParams;

    @Before
    public void before() {
        routeDefinitionWithZeroParams = new RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello",
                dummyHandler,
                AppiumParams.class
        );
        routeDefinitionWithOneParam = new RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello/:param/world",
                dummyHandler,
                AppiumParams.class
        );
        routeDefinitionWithTwoParams = new RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello/:paramOne/something/:paramTwo/world",
                dummyHandler,
                AppiumParams.class
        );
    }

    @Test
    public void shouldProperlyParamsRoutesToUri() throws Exception {
        assertTrue(routeDefinitionWithZeroParams.isMatch("/hello"));
        assertTrue(routeDefinitionWithZeroParams.isMatch("/hello/"));
        assertFalse(routeDefinitionWithZeroParams.isMatch("/hello/world"));
        assertFalse(routeDefinitionWithZeroParams.isMatch("/hellooooooooo"));

        assertTrue(routeDefinitionWithOneParam.isMatch("/hello/something/world"));
        assertTrue(routeDefinitionWithOneParam.isMatch("/hello/something/world/"));
        assertFalse(routeDefinitionWithOneParam.isMatch("/hello/something/something/world"));
        assertFalse(routeDefinitionWithOneParam.isMatch("/hello/world"));
        assertFalse(routeDefinitionWithOneParam.isMatch("/something/hello/something/world"));

        assertTrue(routeDefinitionWithTwoParams.isMatch("/hello/paramOne/something/paramTwo/world"));
        assertTrue(routeDefinitionWithTwoParams.isMatch("/hello/paramOne/something/paramTwo/world/"));
        assertFalse(routeDefinitionWithTwoParams.isMatch("/hello/paramOne/world"));
        assertFalse(routeDefinitionWithTwoParams.isMatch("/hello/paramOne/paramTwo/world"));
        assertFalse(routeDefinitionWithTwoParams.isMatch("/hello/paramOne/something/world"));
    }

    @Test
    public void shouldProperlyParseUriParameters() throws Exception {
        assertEquals("something", routeDefinitionWithOneParam.getUriParams("/hello/something/world").get("param"));
        assertTrue("something", routeDefinitionWithZeroParams.getUriParams("/hello").isEmpty());
        assertEquals("p1", routeDefinitionWithTwoParams.getUriParams("/hello/p1/something/p2/world/").get("paramOne"));
        assertEquals("p2", routeDefinitionWithTwoParams.getUriParams("/hello/p1/something/p2/world/").get("paramTwo"));
    }
}