package io.appium.espressoserver.test.model.web

import com.google.gson.Gson
import io.appium.espressoserver.lib.model.web.WebAtoms
import io.appium.espressoserver.lib.model.web.WebAtoms.WebAtomsMethod
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

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
        assertEquals(webAtomsMethod.atom!!.name, "findElement");
        assertEquals(webAtomsMethod.atom!!.args, mutableListOf("id", "text_input"));
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
        val webAtoms = g.fromJson(json, WebAtoms::class.java)
        assertEquals(webAtoms.webviewElement, "abc")
        assertEquals(webAtoms.forceJavascriptEnabled, true)
        assertEquals(webAtoms.methodChain.get(0).name, "withElement")
        assertEquals(webAtoms.methodChain.get(0).atom!!.name, "findElement")
        assertEquals(webAtoms.methodChain.get(0).atom!!.args, mutableListOf("id", "text_input"))

        assertEquals(webAtoms.methodChain.get(1).name, "perform")
        assertEquals(webAtoms.methodChain.get(1).atom!!.name, "clearElement")
        assertEquals(webAtoms.methodChain.get(1).atom!!.args, Collections.emptyList())

        assertEquals(webAtoms.methodChain.get(2).name, "perform")
        assertEquals(webAtoms.methodChain.get(2).atom!!.name, "webKeys")
        assertEquals(webAtoms.methodChain.get(2).atom!!.args, mutableListOf("Foo"))

    }
}