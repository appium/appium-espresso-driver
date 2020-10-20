package io.appium.espressoserver.test.http

import fi.iki.elonen.NanoHTTPD
import io.appium.espressoserver.lib.handlers.RequestHandler
import io.appium.espressoserver.lib.http.RouteDefinition
import io.appium.espressoserver.lib.model.AppiumParams
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DummyHandler: RequestHandler<AppiumParams, Void?> {
    override fun handle(params: AppiumParams): Void? {
        return null
    }

    override fun handleInternal(params: AppiumParams): Void? {
        return null
    }
}

class RouteDefinitionTest {
    private val dummyHandler = DummyHandler()
    private var routeDefinitionWithOneParam: RouteDefinition? = null
    private var routeDefinitionWithZeroParams: RouteDefinition? = null
    private var routeDefinitionWithTwoParams: RouteDefinition? = null

    @Before
    fun before() {
        routeDefinitionWithZeroParams = RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello",
                dummyHandler,
                AppiumParams::class.java
        )
        routeDefinitionWithOneParam = RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello/:param/world",
                dummyHandler,
                AppiumParams::class.java
        )
        routeDefinitionWithTwoParams = RouteDefinition(
                NanoHTTPD.Method.GET,
                "/hello/:paramOne/something/:paramTwo/world",
                dummyHandler,
                AppiumParams::class.java
        )
    }

    @Test
    fun shouldProperlyParamsRoutesToUri() {
        Assert.assertTrue(routeDefinitionWithZeroParams!!.isMatch("/hello"))
        Assert.assertTrue(routeDefinitionWithZeroParams!!.isMatch("/hello/"))
        Assert.assertFalse(routeDefinitionWithZeroParams!!.isMatch("/hello/world"))
        Assert.assertFalse(routeDefinitionWithZeroParams!!.isMatch("/hellooooooooo"))
        Assert.assertTrue(routeDefinitionWithOneParam!!.isMatch("/hello/something/world"))
        Assert.assertTrue(routeDefinitionWithOneParam!!.isMatch("/hello/something/world/"))
        Assert.assertFalse(routeDefinitionWithOneParam!!.isMatch("/hello/something/something/world"))
        Assert.assertFalse(routeDefinitionWithOneParam!!.isMatch("/hello/world"))
        Assert.assertFalse(routeDefinitionWithOneParam!!.isMatch("/something/hello/something/world"))
        Assert.assertTrue(routeDefinitionWithTwoParams!!.isMatch("/hello/paramOne/something/paramTwo/world"))
        Assert.assertTrue(routeDefinitionWithTwoParams!!.isMatch("/hello/paramOne/something/paramTwo/world/"))
        Assert.assertFalse(routeDefinitionWithTwoParams!!.isMatch("/hello/paramOne/world"))
        Assert.assertFalse(routeDefinitionWithTwoParams!!.isMatch("/hello/paramOne/paramTwo/world"))
        Assert.assertFalse(routeDefinitionWithTwoParams!!.isMatch("/hello/paramOne/something/world"))
    }

    @Test
    fun shouldProperlyParseUriParameters() {
        Assert.assertEquals("something", routeDefinitionWithOneParam!!.getUriParams("/hello/something/world")["param"])
        Assert.assertTrue("something", routeDefinitionWithZeroParams!!.getUriParams("/hello").isEmpty())
        Assert.assertEquals("p1", routeDefinitionWithTwoParams!!.getUriParams("/hello/p1/something/p2/world/")["paramOne"])
        Assert.assertEquals("p2", routeDefinitionWithTwoParams!!.getUriParams("/hello/p1/something/p2/world/")["paramTwo"])
    }
}
