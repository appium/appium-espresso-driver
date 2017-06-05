package io.appium.espressoserver.test.http;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.handlers.RequestHandler;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.http.RouteDefinition;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class RouteDefinitionTest {
    private final RequestHandler<AppiumParams, Void> dummyHandler = new RequestHandler<AppiumParams, Void>() {
        @Override
        public Void handle(AppiumParams params) throws AppiumException {
            return null;
        }
    };

    private RouteDefinition routeDefinitionWithOneParam;
    private RouteDefinition routeDefinitionWithTwoParams;
    private RouteDefinition routeDefinitionWithNoParams;

    @Before
    public void before() {
        routeDefinitionWithOneParam = new RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello/:param/world",
                dummyHandler,
                AppiumParams.class
        );
        routeDefinitionWithTwoParams = new RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello",
                dummyHandler,
                AppiumParams.class
        );
        routeDefinitionWithNoParams = new RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello/:paramOne/something/:paramTwo/world",
                dummyHandler,
                AppiumParams.class
        );
    }

    @Test
    public void shouldProperlyParamsRoutesToUri() throws Exception {
        assertTrue(routeDefinitionWithOneParam.isMatch("/hello/something/world"));
        assertTrue(routeDefinitionWithOneParam.isMatch("/hello/something/world/"));
        assertFalse(routeDefinitionWithOneParam.isMatch("/hello/something/something/world"));
        assertFalse(routeDefinitionWithOneParam.isMatch("/hello/world"));
        assertFalse(routeDefinitionWithOneParam.isMatch("/something/hello/something/world"));

        assertTrue(routeDefinitionWithTwoParams.isMatch("/hello"));
        assertTrue(routeDefinitionWithTwoParams.isMatch("/hello/"));
        assertFalse(routeDefinitionWithTwoParams.isMatch("/hello/world"));
        assertFalse(routeDefinitionWithTwoParams.isMatch("/hellooooooooo"));

        assertTrue(routeDefinitionWithNoParams.isMatch("/hello/paramOne/something/paramTwo/world"));
        assertTrue(routeDefinitionWithNoParams.isMatch("/hello/paramOne/something/paramTwo/world/"));
        assertFalse(routeDefinitionWithNoParams.isMatch("/hello/paramOne/world"));
        assertFalse(routeDefinitionWithNoParams.isMatch("/hello/paramOne/paramTwo/world"));
        assertFalse(routeDefinitionWithNoParams.isMatch("/hello/paramOne/something/world"));
    }

    @Test
    public void shouldProperlyParseUriParameters() throws Exception {
        assertEquals("something", routeDefinitionWithOneParam.getUriParams("/hello/something/world").get("param"));
        assertTrue("something", routeDefinitionWithTwoParams.getUriParams("/hello").isEmpty());
        assertEquals("p1", routeDefinitionWithNoParams.getUriParams("/hello/p1/something/p2/world/").get("paramOne"));
        assertEquals("p2", routeDefinitionWithNoParams.getUriParams("/hello/p1/something/p2/world/").get("paramTwo"));
    }
}