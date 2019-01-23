package io.appium.espressoserver.test.model

import com.google.gson.Gson
import io.appium.espressoserver.lib.model.DataMatcherJson
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class `DataMatcherJson Tests` {
    val g = Gson();

    @Test
    fun `should parse data matchers`() {
        val dataMatcher = g.fromJson("""{
            "name": "instanceOf", "args": "String.class"
        }""".trimIndent(), DataMatcherJson::class.java)
        assertTrue(dataMatcher.matcher.matches("A STRING"))
        assertFalse(dataMatcher.matcher.matches(100))
    }
}