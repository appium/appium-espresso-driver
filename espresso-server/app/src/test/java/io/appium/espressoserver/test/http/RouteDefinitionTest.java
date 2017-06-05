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
    private final RequestHandler<AppiumParams, Void> dumbHandler = new RequestHandler<AppiumParams, Void>() {
        @Override
        public Void handle(AppiumParams params) throws AppiumException {
            return null;
        }
    };

    private RouteDefinition routeDefinitionOne;
    private RouteDefinition routeDefinitionTwo;
    private RouteDefinition routeDefinitionThree;

    @Before
    public void before() {
        routeDefinitionOne = new RouteDefinition(NanoHTTPD.Method.GET, "/hello/:param/world", dumbHandler, AppiumParams.class);
        routeDefinitionTwo = new RouteDefinition(NanoHTTPD.Method.GET, "/hello", dumbHandler, AppiumParams.class);
        routeDefinitionThree = new RouteDefinition(NanoHTTPD.Method.GET, "/hello/:paramOne/something/:paramTwo/world", dumbHandler, AppiumParams.class);
    }

    @Test
    public void isMatchTest() throws Exception {
        assertTrue(routeDefinitionOne.isMatch("/hello/something/world"));
        assertTrue(routeDefinitionOne.isMatch("/hello/something/world/"));
        assertFalse(routeDefinitionOne.isMatch("/hello/something/something/world"));
        assertFalse(routeDefinitionOne.isMatch("/hello/world"));
        assertFalse(routeDefinitionOne.isMatch("/something/hello/something/world"));

        assertTrue(routeDefinitionTwo.isMatch("/hello"));
        assertTrue(routeDefinitionTwo.isMatch("/hello/"));
        assertFalse(routeDefinitionTwo.isMatch("/hello/world"));
        assertFalse(routeDefinitionTwo.isMatch("/hellooooooooo"));

        assertTrue(routeDefinitionThree.isMatch("/hello/paramOne/something/paramTwo/world"));
        assertTrue(routeDefinitionThree.isMatch("/hello/paramOne/something/paramTwo/world/"));
        assertFalse(routeDefinitionThree.isMatch("/hello/paramOne/world"));
        assertFalse(routeDefinitionThree.isMatch("/hello/paramOne/paramTwo/world"));
        assertFalse(routeDefinitionThree.isMatch("/hello/paramOne/something/world"));
    }

    @Test
    public void getUriParamsTest() throws Exception {
        assertEquals("something", routeDefinitionOne.getUriParams("/hello/something/world").get("param"));
        assertTrue("something", routeDefinitionTwo.getUriParams("/hello").isEmpty());
        assertEquals("p1", routeDefinitionThree.getUriParams("/hello/p1/something/p2/world/").get("paramOne"));
        assertEquals("p2", routeDefinitionThree.getUriParams("/hello/p1/something/p2/world/").get("paramTwo"));
    }
}