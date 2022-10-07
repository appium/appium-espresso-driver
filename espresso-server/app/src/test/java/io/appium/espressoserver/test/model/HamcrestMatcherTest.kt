package io.appium.espressoserver.test.model

import androidx.test.espresso.matcher.CursorMatchers
import androidx.test.espresso.matcher.CursorMatchers.CursorMatcher
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.appium.espressoserver.lib.model.HamcrestMatcher
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HamcrestMatcherTest {
    val g = Gson()

    @Test
    fun `should parse Hamcrest matcher with single string arg`() {
        val result = g.fromJson("""
            {"name": "containsString", "args": "Hello World!"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(result.name, "containsString")
        assertTrue(result.args contentEquals arrayOf<Any?>("Hello World!"))
    }

    @Test
    fun `should parse Hamcrest matcher with array of primitive args` () {
        val result = g.fromJson("""{
            "name": "fakeMatcher", 
            "args": [1, true, "Hello World!", null],
            "scope": {
                "name": "isDialog",
                "class": "androidx.test.espresso.matcher.RootMatchers",
                "args": [1]
            }
        }""".trimIndent(), HamcrestMatcher::class.java)
        assertEquals(result.name, "fakeMatcher")
        val numberArg = result.args[0]
        assertTrue(numberArg is Number)
        assertEquals(numberArg.toInt(), 1)
        assertEquals(result.args[1], true)
        assertEquals(result.args[2], "Hello World!")
        assertEquals(result.args[3], null)
        assertEquals("isDialog", result.scope?.name)
        assertEquals(1, result.scope?.args?.size)
        result.scope?.let { assertEquals(1, it.args[0]) }
    }

    @Test
    fun `should parse empty args` () {
        val result = g.fromJson("""
            {"name": "isAThing"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(result.name, "isAThing")
        assertTrue(result.args contentEquals emptyArray())
    }

    @Test
    fun `should parse string as matcher with no args` () {
        val result = g.fromJson("arglessMethod", HamcrestMatcher::class.java)
        assertEquals(result.name, "arglessMethod")
        assertTrue(result.args contentEquals emptyArray())
    }

    @Test
    fun `should default the matcher class type to 'org_hamcrest_Matchers'` () {
        val result = g.fromJson("""
            {"name": "containsString", "args": "Hello"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertNull(result.cls)
        val containsStringMatcher = result.invoke().matcher
        assertTrue(containsStringMatcher.matches("Hello World"))
        assertFalse(containsStringMatcher.matches("Goodbye World"))
    }

    @Test
    fun `should parse the matcher class type` () {
        val result = g.fromJson("""
            {"name": "withRowBlob", "class": "androidx.test.espresso.matcher.CursorMatchers"}
        """.trimIndent(), HamcrestMatcher::class.java)
        assertEquals(result.cls, CursorMatchers::class)
    }

    @Test
    fun `should use 'androidx_test_espresso_matcher' when class provided but package not provided` () {
        val result = g.fromJson("""
            {"name": "withRowDouble", "args": ["Hello", 2.0], "class": "CursorMatchers"}
        """.trimIndent(), HamcrestMatcher::class.java)
        val matcher = result.invoke().matcher
        assertTrue(matcher is CursorMatcher)
    }

    @Test
    fun `should parse matchers that have Class as an arg` () {
        val result = g.fromJson("""
            {"name": "instanceOf", "args": "String.class"}
        """.trimIndent(), HamcrestMatcher::class.java)
        val matcher = result.invoke().matcher
        assertTrue(matcher.matches("Hello World"))
        assertFalse(matcher.matches(123))
    }

    @Test
    fun `should parse nested Hamcrest matchers` () {
        val result = g.fromJson("""
            {"name": "anyOf", "args": [
                {"name": "containsString", "args": "Hello"},
                {"name": "instanceOf", "args": "Integer"}
        ]}""".trimIndent(), HamcrestMatcher::class.java)
        val nestedMatcher = result.invoke().matcher
        assertTrue(nestedMatcher.matches("Hello"))
        assertTrue(nestedMatcher.matches(100))
        assertFalse(nestedMatcher.matches("World"))
        assertFalse(nestedMatcher.matches(100.1))
    }

    @Test
    fun `should parse nested Hamcrest matchers with scope` () {
        val result = g.fromJson("""
            {"name": "anyOf", "args": [
                {"name": "containsString", "args": "Hello"},
                {"name": "instanceOf", "args": "Integer"}
            ], "scope": {
                "name": "isDialog",
                "class": "androidx.test.espresso.matcher.RootMatchers"
            }
        }""".trimIndent(), HamcrestMatcher::class.java)
        val nestedScope = result.invoke()
        assertTrue(nestedScope.matcher.matches("Hello"))
        assertTrue(nestedScope.matcher.matches(100))
        assertFalse(nestedScope.matcher.matches("World"))
        assertFalse(nestedScope.matcher.matches(100.1))
        assertNotNull(nestedScope.scope)
    }

    @Test(expected = JsonParseException::class)
    fun `should fail if name not provided` () {
        g.fromJson("""
            {"args": "Hello World!"}
        """.trimIndent(), HamcrestMatcher::class.java)
    }

    @Test
    fun `should parse Hamcrest matcher that have regex as an arg` () {
        val result = g.fromJson("""
            {"name": "matchesRegex", "args": "[A-Za-z ]*"}
        """.trimIndent(), HamcrestMatcher::class.java)
        val matcher = result.invoke().matcher
        assertTrue(matcher.matches("Hello World"))
        assertFalse(matcher.matches("Hello World!"))
    }
}
