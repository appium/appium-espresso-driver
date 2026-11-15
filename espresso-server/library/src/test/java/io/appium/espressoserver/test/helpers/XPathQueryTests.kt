/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.test.helpers

import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException
import io.appium.espressoserver.lib.model.compileXpathQuery
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

const val PAGE_SOURCE = """<hierarchy>
  <android.widget.FrameLayout viewIndex="0" text="Hello">
    <android.widget.TextView viewIndex="1" text="Apple" enabled="true"/>
    <android.widget.Button viewIndex="2" text="banana" enabled="false"/>
  </android.widget.FrameLayout>
</hierarchy>"""

fun evaluate(xpath: String): List<Int> =
    compileXpathQuery(xpath).evaluateNodeIds(PAGE_SOURCE.byteInputStream(), "viewIndex")

class `XPath Query Tests` {

    @Test
    fun `should find elements by an XPath 1 expression`() {
        assertEquals(listOf(1), evaluate("//android.widget.TextView[@text='Apple']"))
        assertEquals(listOf(1, 2), evaluate("//*[@enabled and @text]"))
        assertEquals(listOf(2), evaluate("//android.widget.FrameLayout/*[last()]"))
    }

    @Test
    fun `should return no matches if nothing is found`() {
        assertEquals(emptyList(), evaluate("//android.widget.Switch"))
    }

    @Test
    fun `should report malformed expressions as invalid selector`() {
        assertFailsWith<XPathLookupException> { compileXpathQuery("//*[") }
    }

    @Test
    fun `should support XPath 2 functions`() {
        assertEquals(listOf(0, 1), evaluate("//*[matches(@text, '^[A-Z]')]"))
        assertEquals(listOf(2), evaluate("//*[lower-case(@text) = 'banana']"))
    }

    @Test
    fun `should support sequences and FLWOR expressions`() {
        assertEquals(listOf(1, 2), evaluate("//*[@text = ('Apple', 'banana')]"))
        assertEquals(listOf(2), evaluate("for ${"$"}b in //android.widget.Button return ${"$"}b"))
    }

    @Test
    fun `should skip non-element results`() {
        assertEquals(emptyList(), evaluate("//@text"))
    }

    @Test
    fun `should report dynamic errors as invalid selector`() {
        val error = assertFailsWith<XPathLookupException> { evaluate("//*[@text + 1 = 2]") }
        assertContains(error.message.orEmpty(), "Could not evaluate XPath")
    }

    @Test
    fun `should report syntax errors as parse errors`() {
        val error = assertFailsWith<XPathLookupException> { compileXpathQuery("//*[") }
        assertContains(error.message.orEmpty(), "Could not parse XPath")
    }

    @Test
    fun `should report unknown functions and unbound variables as invalid selector`() {
        assertFailsWith<XPathLookupException> { compileXpathQuery("//*[unknown()]") }
        assertFailsWith<XPathLookupException> { compileXpathQuery("//*[@text = ${"$"}missing]") }
    }

    @Test
    fun `should support the standard xs and fn prefixes`() {
        assertEquals(listOf(1, 2), evaluate("//*[@enabled][xs:boolean(@enabled) or true()]"))
        assertEquals(listOf(0, 1), evaluate("//*[fn:matches(@text, '^[A-Z]')]"))
        assertEquals(listOf(0), evaluate("//*[xs:string(@text) = 'Hello']"))
    }

    @Test
    fun `should not load documents with fn doc`() {
        assertFailsWith<XPathLookupException> { evaluate("doc('file:///etc/hosts')") }
    }
}
