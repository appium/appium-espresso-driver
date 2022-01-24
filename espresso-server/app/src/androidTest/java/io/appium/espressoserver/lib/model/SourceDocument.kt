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

import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import android.util.SparseArray
import android.util.Xml
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.SelectionResult
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onRoot
import io.appium.espressoserver.EspressoServerRunnerTest
import io.appium.espressoserver.EspressoServerRunnerTest.Companion.context
import io.appium.espressoserver.lib.drivers.DriverContext
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.StringHelpers.abbreviate
import io.appium.espressoserver.lib.helpers.XMLHelpers.toNodeName
import io.appium.espressoserver.lib.helpers.XMLHelpers.toSafeString
import io.appium.espressoserver.lib.helpers.extensions.withPermit
import io.appium.espressoserver.lib.viewaction.ViewGetter
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.lang.AssertionError
import java.util.*
import java.util.concurrent.Semaphore
import javax.xml.xpath.*
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

const val NON_XML_CHAR_REPLACEMENT = "?"
const val VIEW_INDEX = "viewIndex"
const val NAMESPACE = ""
val DEFAULT_VIEW_CLASS_NAME = View::javaClass.name
const val MAX_TRAVERSAL_DEPTH = 70
const val MAX_XML_VALUE_LENGTH = 64 * 1024
const val XML_ENCODING = "UTF-8"
val XPATH: XPath = XPathFactory.newInstance().newXPath()


private fun toXmlNodeName(className: String?): String {
    if (className == null || className.trim { it <= ' ' }.isEmpty()) {
        return DEFAULT_VIEW_CLASS_NAME
    }

    var fixedName = className
        .replace("[$@#&]".toRegex(), ".")
        .replace("\\.+".toRegex(), ".")
        .replace("(^\\.|\\.$)".toRegex(), "")
    fixedName = toNodeName(fixedName)
    if (fixedName.trim { it <= ' ' }.isEmpty()) {
        fixedName = DEFAULT_VIEW_CLASS_NAME
    }
    if (fixedName != className) {
        AndroidLogger.info("Rewrote class name '$className' to XML node name '$fixedName'")
    }
    return fixedName
}


class SourceDocument constructor(
    private val root: Any? = null,
    private val includedAttributes: Set<AttributesEnum>? = null
) {
    @Suppress("PrivatePropertyName")
    private val RESOURCES_GUARD = Semaphore(1)

    private val viewMap: SparseArray<View> = SparseArray()
    private var serializer: XmlSerializer? = null
    private var tmpXmlName: String? = null

    private fun setAttribute(attrName: AttributesEnum, attrValue: Any?) {
        // Do not write attributes, whose values equal to null
        attrValue?.let {
            // Cut off longer strings to avoid OOM errors
            val xmlValue = abbreviate(toSafeString(it.toString(), NON_XML_CHAR_REPLACEMENT),
                    MAX_XML_VALUE_LENGTH)
            serializer?.attribute(NAMESPACE, attrName.toString(), xmlValue)
        }
    }

    private fun recordAdapterViewInfo(adapterView: AdapterView<*>) {
        val adapter = adapterView.adapter ?: return
        val adapterCount = adapter.count
        val adapterData = ArrayList<String>()
        var isAdapterTypeSet = false
        for (i in 0 until adapterCount) {
            val adapterItem = adapter.getItem(i) ?: continue
            adapterData.add(adapterItem.toString())

            // Get the type of the adapter item
            if (!isAdapterTypeSet) {
                setAttribute(AttributesEnum.ADAPTER_TYPE, adapterItem.javaClass.simpleName)
                isAdapterTypeSet = true
            }
        }
        if (adapterData.isNotEmpty()) {
            setAttribute(AttributesEnum.ADAPTERS, TextUtils.join(",", adapterData))
        }
    }

    private fun isAttributeIncluded(attr: AttributesEnum): Boolean =
        null == includedAttributes || includedAttributes.contains(attr)

    /**
     * Recursively visit all of the views and map them to XML elements
     *
     * @param view  The root view
     * @param depth The current traversal depth
     */
    private fun serializeView(view: View?, depth: Int) {
        if (view == null) {
            return
        }

        val viewElement = ViewElement(view)
        val className = viewElement.className
        val tagName = toXmlNodeName(className)
        serializer?.startTag(NAMESPACE, tagName)

        var isTextOrHintRecorded = false
        var isAdapterInfoRecorded = false
        linkedMapOf(
            AttributesEnum.INDEX to { viewElement.index },
            AttributesEnum.PACKAGE to { viewElement.packageName },
            AttributesEnum.CLASS to { className },
            AttributesEnum.CONTENT_DESC to { viewElement.contentDescription },
            AttributesEnum.CHECKABLE to { viewElement.isCheckable },
            AttributesEnum.CHECKED to { viewElement.isChecked },
            AttributesEnum.CLICKABLE to { viewElement.isClickable },
            AttributesEnum.ENABLED to { viewElement.isEnabled },
            AttributesEnum.FOCUSABLE to { viewElement.isFocusable },
            AttributesEnum.FOCUSED to { viewElement.isFocused },
            AttributesEnum.SCROLLABLE to { viewElement.isScrollable },
            AttributesEnum.LONG_CLICKABLE to { viewElement.isLongClickable },
            AttributesEnum.PASSWORD to { viewElement.isPassword },
            AttributesEnum.SELECTED to { viewElement.isSelected },
            AttributesEnum.VISIBLE to { viewElement.isVisible },
            AttributesEnum.BOUNDS to { viewElement.bounds.toShortString() },
            AttributesEnum.TEXT to null,
            AttributesEnum.HINT to null,
            AttributesEnum.RESOURCE_ID to { viewElement.resourceId },
            AttributesEnum.VIEW_TAG to { viewElement.viewTag },
            AttributesEnum.ADAPTERS to null,
            AttributesEnum.ADAPTER_TYPE to null
        ).forEach {
            when (it.key) {
                AttributesEnum.TEXT, AttributesEnum.HINT ->
                    if (!isTextOrHintRecorded && isAttributeIncluded(it.key)) {
                        viewElement.text?.let { text ->
                            setAttribute(AttributesEnum.TEXT, text.rawText)
                            setAttribute(AttributesEnum.HINT, text.isHint)
                            isTextOrHintRecorded = true
                        }
                    }
                AttributesEnum.ADAPTERS, AttributesEnum.ADAPTER_TYPE ->
                    if (!isAdapterInfoRecorded && view is AdapterView<*> && isAttributeIncluded(it.key)) {
                        recordAdapterViewInfo(view)
                        isAdapterInfoRecorded = true
                    }
                else -> if (isAttributeIncluded(it.key)) {
                    setAttribute(it.key, it.value!!())
                }
            }
        }

        serializer?.attribute(NAMESPACE, VIEW_INDEX, viewMap.size().toString())
        viewMap.put(viewMap.size(), view)

        if (depth < MAX_TRAVERSAL_DEPTH) {
            // Visit the children and build them too
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) {
                    serializeView(view.getChildAt(index), depth + 1)
                }
            }
        } else {
            AndroidLogger.warn(
                "Skipping traversal of ${view.javaClass.name}'s children, since " +
                        "the current depth has reached its maximum allowed value of $depth"
            )
        }

        serializer?.endTag(NAMESPACE, tagName)
    }

    private fun serializeComposeNode(semanticsNode: SemanticsNode?, depth: Int) {
        if (semanticsNode == null) {
            return
        }
        val nodeElement = ComposeNodeElement(semanticsNode)
        val className = nodeElement.className
        val tagName = toXmlNodeName(className)
        serializer?.startTag(NAMESPACE, tagName)

        linkedMapOf(
            AttributesEnum.CLASS to { className },
            AttributesEnum.INDEX to { nodeElement.index },
            AttributesEnum.CLICKABLE to { nodeElement.isClickable },
            AttributesEnum.ENABLED to { nodeElement.isEnabled },
            AttributesEnum.FOCUSED to { nodeElement.isFocused },
            AttributesEnum.SCROLLABLE to { nodeElement.isScrollable },
            AttributesEnum.SELECTED to { nodeElement.isSelected },
            AttributesEnum.CHECKED to { nodeElement.isChecked },
            AttributesEnum.VIEW_TAG to { nodeElement.viewTag },
            AttributesEnum.CONTENT_DESC to { nodeElement.contentDescription },
            AttributesEnum.BOUNDS to { nodeElement.bounds.toShortString() },
            AttributesEnum.TEXT to { nodeElement.text },
            AttributesEnum.PASSWORD to { nodeElement.isPassword },
            AttributesEnum.RESOURCE_ID to { nodeElement.resourceId },
        ).forEach {
            setAttribute(it.key, it.value())
        }

        if (depth < MAX_TRAVERSAL_DEPTH) {
            // Visit the children and build them too
            for (index in 0 until semanticsNode.children.count()) {
                serializeComposeNode(semanticsNode.children[index], depth + 1)
            }
        } else {
            AndroidLogger.warn(
                "Skipping traversal of ${semanticsNode.javaClass.name}'s children, since " +
                        "the current depth has reached its maximum allowed value of $depth"
            )
        }

        serializer?.endTag(NAMESPACE, tagName)
    }

    private fun toStream(): InputStream {
        var lastError: Throwable? = null
        // Try to serialize the xml into the memory first, since it is fast
        // Switch to a file system serializer if the first approach causes OutOfMemory
        for (streamType in arrayOf<Class<*>>(
            ByteArrayOutputStream::class.java,
            FileOutputStream::class.java
        )) {
            serializer = Xml.newSerializer()
            viewMap.clear()

            try {
                val outputStream = if (streamType == FileOutputStream::class.java) {
                    tmpXmlName = "${UUID.randomUUID()}.xml"
                    getApplicationContext<Context>().openFileOutput(
                        tmpXmlName,
                        Context.MODE_PRIVATE
                    )
                } else ByteArrayOutputStream()
                try {
                    serializer?.let {
                        it.setOutput(outputStream, XML_ENCODING)
                        it.startDocument(XML_ENCODING, true)
                        it.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                        val startTime = SystemClock.uptimeMillis()
                        when (context.currentStrategyType) {
                            DriverContext.StrategyType.COMPOSE -> {
                                if (root != null) {
                                    serializeComposeNode(root as SemanticsNode, 0)
                                } else {
                                    val rootNodes = rootSemanticNodes()
                                    if (rootNodes.size == 1) {
                                        serializeComposeNode(rootNodes.first(), 0)
                                    } else {
                                        serializer?.startTag(NAMESPACE, DEFAULT_TAG_NAME)
                                        rootNodes.forEach { semanticsNode -> serializeComposeNode(semanticsNode, 0) }
                                        serializer?.endTag(NAMESPACE, DEFAULT_TAG_NAME)
                                    }
                                }
                            }
                            DriverContext.StrategyType.ESPRESSO -> {
                                val rootView = root ?: ViewGetter().rootView
                                serializeView(rootView as View, 0)
                            }
                        }

                        it.endDocument()
                        AndroidLogger.info(
                            "The source XML tree has been fetched in " +
                                    "${SystemClock.uptimeMillis() - startTime}ms " +
                                    "using ${streamType.simpleName}"
                        )
                    }
                } catch (e: OutOfMemoryError) {
                    lastError = e
                    continue
                } finally {
                    outputStream.close()
                }
                return if (outputStream is FileOutputStream)
                    getApplicationContext<Context>().openFileInput(tmpXmlName)
                else
                    ByteArrayInputStream((outputStream as ByteArrayOutputStream).toByteArray())
            } catch (e: IOException) {
                lastError = e
            }

        }
        if (lastError is OutOfMemoryError) {
            throw lastError
        }
        throw AppiumException(lastError!!)
    }

    private fun rootSemanticNodes(): List<SemanticsNode> {
        return try {
            listOf(EspressoServerRunnerTest.composeTestRule.onRoot(useUnmergedTree = true).fetchSemanticsNode())
        } catch (e: AssertionError) {
//            Ideally there should be on `root` node but on some cases e.g:overlays screen, there can be more than 1 root.
//            Compose API not respecting such cases instead throws AssertionError, as a work around fetching all root nodes by relaying on internal API.
//            e.g: "Reason: Expected exactly '1' node but found '2' nodes that satisfy: (isRoot)"
            val result: SelectionResult = SemanticsNodeInteraction::class.declaredMemberFunctions.find { it.name == "fetchSemanticsNodes" }?.let {
                it.isAccessible = true
                it.call(EspressoServerRunnerTest.composeTestRule.onRoot(useUnmergedTree = true), true, null)
            } as SelectionResult
            result.selectedNodes
        }
    }

    private fun performCleanup() {
        tmpXmlName?.let {
            getApplicationContext<Context>().deleteFile(it)
            tmpXmlName = null
        }
    }

    fun toXMLString(): String {
        return RESOURCES_GUARD.withPermit({
            toStream().use { xmlStream ->
                val sb = StringBuilder()
                val reader = BufferedReader(InputStreamReader(xmlStream, XML_ENCODING))
                var line: String? = reader.readLine()
                while (line != null) {
                    sb.append(line)
                    line = reader.readLine()
                }
                sb.toString()
            }
        }, { performCleanup() })
    }

    fun findViewsByXPath(xpathSelector: String): List<View> =
        matchingNodeIds(xpathSelector, VIEW_INDEX).map { viewMap.get(it) }

    fun matchingNodeIds(xpathSelector: String, attributeName: String): List<Int> {
        val expr = try {
            XPATH.compile(xpathSelector)
        } catch (xe: XPathExpressionException) {
            throw XPathLookupException(xpathSelector, xe.message!!)
        }
        return RESOURCES_GUARD.withPermit({
            toStream().use { xmlStream ->
                val list = expr.evaluate(InputSource(xmlStream), XPathConstants.NODESET) as NodeList
                (0 until list.length).map { index ->
                    list.item(index).attributes.getNamedItem(attributeName).nodeValue.toInt()
                }
            }
        }, { performCleanup() })
    }
}
