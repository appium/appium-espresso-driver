package io.appium.espressoserver.test.helpers.w3c

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import org.junit.Test

import io.appium.espressoserver.lib.helpers.w3c.caps.parseCapabilities

import org.junit.Assert.*

class CapsUtilsTest {

    @Test
    fun shouldParseValidW3CCapabilitiesFromFirstMatch() {
        val parsedCaps = parseCapabilities(listOf(mapOf(
               "appActivity" to "com.package.activity",
               "appPackage" to "com.package"
               )), null)
        assertEquals(parsedCaps["appActivity"], "com.package.activity")
        assertEquals(parsedCaps["appPackage"], "com.package")
    }

    @Test
    fun shouldParseValidEmptyW3CCapabilities() {
        val parsedCaps = parseCapabilities(null, null)
        assertEquals(parsedCaps.size, 0)
    }

    @Test
    fun shouldParseValidW3CCapabilitiesFromAlwaysMatch() {
        val parsedCaps = parseCapabilities(listOf(), mapOf(
                "appActivity" to "com.package.activity",
                "appPackage" to "com.package"
        ))
        assertEquals(parsedCaps["appActivity"], "com.package.activity")
        assertEquals(parsedCaps["appPackage"], "com.package")
    }

    @Test
    fun shouldParseValidW3CCapabilitiesFromBothMatches() {
        val parsedCaps = parseCapabilities(listOf(mapOf(
                "appActivity" to "com.package.activity"
        )), mapOf(
                "appPackage" to "com.package"
        ))
        assertEquals(parsedCaps["appActivity"], "com.package.activity")
        assertEquals(parsedCaps["appPackage"], "com.package")
    }

    @Test
    fun shouldParseValidW3CCapabilitiesFromBothMatchesWithPrefixes() {
        val parsedCaps = parseCapabilities(listOf(mapOf(
                "appium:appActivity" to "com.package.activity"
        )), mapOf(
                "appium:appPackage" to "com.package"
        ))
        assertEquals(parsedCaps["appActivity"], "com.package.activity")
        assertEquals(parsedCaps["appPackage"], "com.package")
    }

    @Test(expected = InvalidArgumentException::class)
    fun shouldThrowInvalidArgumentIfPropertyExistsInBothFirstAndAlwaysMatch() {
        parseCapabilities(listOf(mapOf(
                "appium:appActivity" to "com.package.activity"
        )), mapOf(
                "appActivity" to "com.package.activity",
                "appium:appPackage" to "com.package"
        ))
    }

    @Test(expected = InvalidArgumentException::class)
    fun shouldThrowInvalidArgumentIfStandardPropertyIsPrefixed() {
        parseCapabilities(listOf(mapOf(
                "appium:platformName" to "android"
        )), null)
    }
}
