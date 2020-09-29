package io.appium.espressoserver.test.model

import androidx.test.espresso.matcher.CursorMatchers
import androidx.test.espresso.matcher.CursorMatchers.CursorMatcher
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.appium.espressoserver.lib.model.HamcrestMatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HamcrestMatcherTest {
    val g = Gson()

    @Test
    fun `should parse Hamcrest matcher with single string arg`() {
        val matcher = g.fromJson("""
            {"name": "containsString", "args": "Hello World!"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.name, "containsString")
        assertTrue(matcher.args contentEquals arrayOf<Any?>("Hello World!"))
    }

    @Test
    fun `should parse Hamcrest matcher with array of primitive args` () {
        val matcher = g.fromJson("""
            {"name": "fakeMatcher", "args": [1, true, "Hello World!", null]}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.name, "fakeMatcher")
        val numberArg = matcher.args[0]
        assertTrue(numberArg is Number)
        assertEquals(numberArg.toInt(), 1)
        assertEquals(matcher.args[1], true)
        assertEquals(matcher.args[2], "Hello World!")
        assertEquals(matcher.args[3], null)
    }

    @Test
    fun `should parse empty args` () {
        val matcher = g.fromJson("""
            {"name": "isAThing"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.name, "isAThing")
        assertTrue(matcher.args contentEquals emptyArray())
    }

    @Test
    fun `should parse string as matcher with no args` () {
        val matcher = g.fromJson("arglessMethod", HamcrestMatcher::class.java)
        assertEquals(matcher.name, "arglessMethod")
        assertTrue(matcher.args contentEquals emptyArray())
    }

    @Test
    fun `should default the matcher class type to 'org_hamcrest_Matchers'` () {
        val matcher = g.fromJson("""
            {"name": "containsString", "args": "Hello"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.matcherClass, org.hamcrest.Matchers::class)
        val containsStringMatcher = matcher.invoke()
        assertTrue(containsStringMatcher.matches("Hello World"))
        assertFalse(containsStringMatcher.matches("Goodbye World"))
    }

    @Test
    fun `should parse the matcher class type` () {
        val matcher = g.fromJson("""
            {"name": "withRowBlob", "class": "androidx.test.espresso.matcher.CursorMatchers"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.matcherClass, CursorMatchers::class)
    }

    @Test
    fun `should use 'androidx_test_espresso_matcher' when class provided but package not provided` () {
        val matcher = g.fromJson("""
            {"name": "withRowDouble", "args": ["Hello", 2.0], "class": "CursorMatchers"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(matcher.matcherClass, CursorMatchers::class)
        assertTrue(matcher.invoke() is CursorMatcher)
    }

    @Test
    fun `should parse matchers that have Class as an arg` () {
        val matcher = g.fromJson("""
            {"name": "instanceOf", "args": "String.class"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertTrue(matcher.invoke().matches("Hello World"))
        assertFalse(matcher.invoke().matches(123))
    }

    @Test
    fun `should parse nested Hamcrest matchers` () {
        val matcher = g.fromJson("""
            {"name": "anyOf", "args": [
                {"name": "containsString", "args": "Hello"},
                {"name": "instanceOf", "args": "Integer"}
        ]}""".trimIndent(), HamcrestMatcher::class.java)
        val nestedMatcher = matcher.invoke()
        assertTrue(nestedMatcher.matches("Hello"))
        assertTrue(nestedMatcher.matches(100))
        assertFalse(nestedMatcher.matches("World"))
        assertFalse(nestedMatcher.matches(100.1))
    }

    @Test(expected = JsonParseException::class)
    fun `should fail if name not provided` () {
        g.fromJson("""
            {"args": "Hello World!"}
        """.trimIndent(), HamcrestMatcher::class.java)
    }
}