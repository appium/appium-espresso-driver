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

package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException
import io.appium.xpath2.DocumentLoader
import org.eclipse.wst.xml.xpath2.api.XPath2Expression
import org.eclipse.wst.xml.xpath2.processor.Engine
import org.eclipse.wst.xml.xpath2.processor.XPathParserException
import org.eclipse.wst.xml.xpath2.processor.util.DynamicContextBuilder
import org.eclipse.wst.xml.xpath2.processor.util.StaticContextBuilder
import org.w3c.dom.Element
import java.io.InputStream

/** A compiled XPath 2.0 expression, which is evaluated against the XML page source. */
internal class XPathQuery(
    private val selector: String,
    private val expression: XPath2Expression,
    private val staticContext: StaticContextBuilder
) {
    /** Returns the integer values of [attributeName] of the matched element nodes. */
    fun evaluateNodeIds(xml: InputStream, attributeName: String): List<Int> {
        val document = DocumentLoader.parse(xml)
        val result = try {
            expression.evaluate(DynamicContextBuilder(staticContext), arrayOf<Any>(document))
        } catch (e: Exception) {
            throw XPathLookupException.evaluationFailure(selector, e.message ?: e.javaClass.simpleName)
        }
        return result.iterator().asSequence()
            .map { it.nativeValue }
            .filterIsInstance<Element>()
            .filter { it.hasAttribute(attributeName) }
            .map { it.getAttribute(attributeName).toInt() }
            .toList()
    }
}

internal fun compileXpathQuery(selector: String): XPathQuery {
    val staticContext = StaticContextBuilder()
    return try {
        XPathQuery(selector, Engine().parseExpression(selector, staticContext), staticContext)
    } catch (e: XPathParserException) {
        throw XPathLookupException(selector, e.message ?: e.javaClass.simpleName)
    }
}
