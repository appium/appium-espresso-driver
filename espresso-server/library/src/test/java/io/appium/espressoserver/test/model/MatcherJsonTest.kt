package io.appium.espressoserver.test.model

import com.google.gson.Gson
import io.appium.espressoserver.lib.model.MatcherJson
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class `MatcherJson Tests` {
    val g = Gson()

    @Test
    fun `should parse matchers as object`() {
        val dataMatcher = g.fromJson("""{
            "name": "instanceOf", "args": "String.class"
        }""".trimIndent(), MatcherJson::class.java)
        assertTrue(dataMatcher.query.matcher.matches("A STRING"))
        assertFalse(dataMatcher.query.matcher.matches(100))
    }
}
