package io.appium.espressoserver.test.helpers

import io.appium.espressoserver.lib.helpers.forNameApp
import io.appium.espressoserver.lib.helpers.forNameShaded
import io.appium.espressoserver.lib.helpers.stripShadedPrefix
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class `ShadingHelpers Test` {

    @Test
    fun `'forNameShaded' falls back to the original name`() {
        assertEquals(String::class.java, forNameShaded("java.lang.String"))
    }

    @Test
    fun `'forNameShaded' throws for unknown classes`() {
        assertFailsWith<ClassNotFoundException> { forNameShaded("no.such.Clazz") }
    }

    @Test
    fun `'forNameApp' prefers the original name`() {
        assertEquals(String::class.java, forNameApp("java.lang.String"))
        assertFailsWith<ClassNotFoundException> { forNameApp("no.such.Clazz") }
    }

    @Test
    fun `'stripShadedPrefix' removes the relocation prefix everywhere`() {
        assertEquals(
            "NoActivityResumedException: androidx.test.espresso.Foo and org.hamcrest.Matchers",
            ("NoActivityResumedException: io.appium.espressoserver.shaded.androidx.test.espresso.Foo " +
                "and io.appium.espressoserver.shaded.org.hamcrest.Matchers").stripShadedPrefix(),
        )
        assertEquals("io.appium.espressoserver.lib.Foo", "io.appium.espressoserver.lib.Foo".stripShadedPrefix())
    }
}
