package io.appium.espressoserver.test.model.web

import androidx.test.espresso.web.webdriver.Locator
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.appium.espressoserver.lib.model.web.WebAtomDeserializer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebAtomTest {

    @Test
    fun `should parse web atom with no args`() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", "clearElement")
        val webAtom = WebAtomDeserializer().deserialize(jsonObject, null, null)
        assertEquals(webAtom.name, "clearElement")
    }

    @Test
    fun `should parse JSON primtive as web atom with name and no args`() {
        val jsonObject = JsonPrimitive("clearElement")
        val webAtom = WebAtomDeserializer().deserialize(jsonObject, null, null)
        assertEquals(webAtom.name, "clearElement")
    }

    @Test
    fun `should parse web atom with singleton args`() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", "webKeys")
        jsonObject.addProperty("args", "Hello World!")
        val webAtom = WebAtomDeserializer().deserialize(jsonObject, null, null)
        assertEquals(webAtom.name, "webKeys")
        assertEquals(webAtom.args.first(), "Hello World!")
        assertEquals(webAtom.args.size, 1)
    }

    @Test
    fun `should parse web atom name with array of args of containing differing JSON primitive types`() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", "someFakeAtom")
        val argsArr = JsonArray()
        argsArr.add(JsonPrimitive("hello"))
        argsArr.add(JsonPrimitive(true))
        argsArr.add(JsonPrimitive(100))
        argsArr.add(JsonPrimitive(1.1))
        jsonObject.add("args", argsArr)
        val webAtom = WebAtomDeserializer().deserialize(jsonObject, null, null)
        assertEquals(webAtom.name, "someFakeAtom")
        assertTrue(webAtom.args contentEquals arrayOf("hello", true, 100, 1.1))
        assertEquals(webAtom.args.size, 4)
    }

    @Test
    fun `should accept locator shorthand in place of "args"`() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", "findElement")
        val locatorObject = JsonObject()
        locatorObject.addProperty("using", "id")
        locatorObject.addProperty("value", "some_html_id")
        jsonObject.add("locator", locatorObject)
        val webAtom = WebAtomDeserializer().deserialize(jsonObject, null, null)
        assertEquals(webAtom.name, "findElement")
        assertTrue(webAtom.args contentEquals arrayOf(Locator.ID, "some_html_id"))
    }
}
