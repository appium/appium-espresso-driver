package io.appium.espressoserver.test.helpers

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import io.appium.espressoserver.lib.helpers.GsonParserHelpers
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GsonParserHelpersTest {
    enum class BasicEnum { A, B, C }

    @Test
    fun shouldParseEnumsFromJsonObj() {
        val jsonObj = JsonObject()
        jsonObj.add("b", JsonPrimitive("b"))
        val enumValue = GsonParserHelpers.parseEnum<BasicEnum>(jsonObj, "b", "")
        assertEquals(enumValue, BasicEnum.B)
    }

    @Test
    fun shouldReturnNullIfNoObject() {
        val jsonObj = JsonObject()
        val enumValue = GsonParserHelpers.parseEnum<BasicEnum>(jsonObj, "b", "")
        assertNull(enumValue)
    }

    @Test
    fun shouldThrowExceptionIfBadValue() {
        try {
            val jsonObj = JsonObject()
            jsonObj.add("b", JsonPrimitive("z"))
            GsonParserHelpers.parseEnum<BasicEnum>(jsonObj, "b", "")
        } catch (jpe:JsonParseException) {
            return assertTrue(true);
        }
        assertTrue(false);
    }
}