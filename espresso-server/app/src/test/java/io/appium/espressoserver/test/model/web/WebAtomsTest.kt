package io.appium.espressoserver.test.model.web

import com.google.gson.Gson
import io.appium.espressoserver.lib.model.web.WebAtomsParams
import io.appium.espressoserver.lib.model.web.WebAtomsMethod
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebAtomsTest {
    val g = Gson()
    @Test
    fun `should parse name and atom for WebAtoms method`() {
        val webAtomsMethod = g.fromJson("""{
          "name": "withElement",
          "atom": {
            "name": "findElement",
            "locator": {
              "using": "id",
              "value": "text_input"
            }
          }
        }""".trimIndent(), WebAtomsMethod::class.java)
        assertEquals(webAtomsMethod.name, "withElement");
        assertEquals(webAtomsMethod.atom.name, "findElement");
        assertTrue(webAtomsMethod.atom.args contentEquals  arrayOf("id", "text_input"));
    }

    @Test
    fun `should parse web atoms object`() {
        val json = """{
           "webviewElement":"abc",
           "forceJavascriptEnabled":true,
           "methodChain":[
              {
                 "name":"withElement",
                 "atom":{
                    "name":"findElement",
                    "locator":{
                       "using":"id",
                       "value":"text_input"
                    }
                 }
              },
              {
                 "name":"perform",
                 "atom":"clearElement"
              },
              {
                 "name":"perform",
                 "atom":{
                    "name":"webKeys",
                    "args":"Foo"
                 }
              }
           ]
        }"""
        val webAtoms = g.fromJson(json, WebAtomsParams::class.java)
        assertEquals(webAtoms.webviewElement, "abc")
        assertEquals(webAtoms.forceJavascriptEnabled, true)

        webAtoms.methodChain.get(0).let {
            assertEquals(it.name, "withElement")
            assertEquals(it.atom.name, "findElement")
            assertTrue(it.atom.args contentEquals arrayOf("id", "text_input"))
        }

        webAtoms.methodChain.get(1).let {
            assertEquals(it.name, "perform")
            assertEquals(it.atom.name, "clearElement")
            assertTrue(it.atom.args contentEquals  emptyArray())
        }

        webAtoms.methodChain.get(2).let {
            assertEquals(it.name, "perform")
            assertEquals(it.atom.name, "webKeys")
            assertTrue(it.atom.args contentEquals  arrayOf("Foo"))
        }

    }
}