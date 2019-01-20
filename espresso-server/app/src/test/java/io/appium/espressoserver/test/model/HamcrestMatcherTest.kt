package io.appium.espressoserver.test.model

import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.appium.espressoserver.lib.model.HamcrestMatcher
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HamcrestMatcherTest {
    val g = Gson();

    @Test
    fun `should parse Hamcrest matcher with single string arg`() {
        val matcher = g.fromJson("""
            {"name": "containsString", "args": "Hello World!"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.name, "containsString")
        assertEquals(matcher.args, arrayListOf("Hello World!"))
    }

    @Test
    fun `should parse Hamcrest matcher with array of primitive args` () {
        val matcher = g.fromJson("""
            {"name": "fakeMatcher", "args": [1, true, "Hello World!", null]}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.name, "fakeMatcher")
        val numberArg = matcher.args.get(0)
        assertTrue(numberArg is Number)
        assertEquals(numberArg.toInt(), 1)
        assertEquals(matcher.args.get(1), true)
        assertEquals(matcher.args.get(2), "Hello World!")
        assertEquals(matcher.args.get(3), null)
    }

    @Test
    fun `should parse Hamcrest matcher with an matcher object as an arg` () {
        val matcher = g.fromJson("""
            {"name": "fakeMatcher", "args": [{
                "name": "nestedFakeMatcher", "args": ["hello"]
            }]}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.name, "fakeMatcher")
        val arg = matcher.args.get(0)
        assertEquals(matcher.name, "fakeMatcher")
        assertTrue(arg is HamcrestMatcher)
        assertEquals(arg.name, "nestedFakeMatcher")
        assertEquals(arg.args.get(0), "hello")
    }

    @Test
    fun `should parse empty args` () {
        val matcher = g.fromJson("""
            {"name": "isAThing"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.name, "isAThing")
        assertEquals(matcher.args, Collections.emptyList())
    }

    @Test
    fun `should parse string as matcher with no args` () {
        val matcher = g.fromJson("arglessMethod", HamcrestMatcher::class.java)
        assertEquals(matcher.name, "arglessMethod")
        assertEquals(matcher.args, Collections.emptyList())
    }

    @Test(expected = JsonParseException::class)
    fun `should fail if name not provided` () {
        val matcher = g.fromJson("""
            {"args": "Hello World!"}
        """.trimIndent(), HamcrestMatcher::class.java)
    }

    @Test(expected = JsonParseException::class)
    fun `should fail if primitive is provided` () {
        val matcher = g.fromJson("""
            true
        """.trimIndent(), HamcrestMatcher::class.java)
    }
}