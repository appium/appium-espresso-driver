package io.appium.espressoserver.test.http

import io.appium.espressoserver.lib.http.Server
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.rules.TestRule

class ServerTest {
    @Test
    fun serverIsUsableAsJUnitRule() {
        // The instrumentation test declares the server as a JUnit rule in every flavor
        assertTrue(TestRule::class.java.isAssignableFrom(Server::class.java))
    }
}
