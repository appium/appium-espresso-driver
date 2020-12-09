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
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.StringHelpers.abbreviate
import io.appium.espressoserver.lib.helpers.XMLHelpers.toNodeName
import io.appium.espressoserver.lib.helpers.XMLHelpers.toSafeString
import io.appium.espressoserver.lib.helpers.extensions.withPermit
import io.appium.espressoserver.lib.viewaction.ViewGetter
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.util.*
import java.util.concurrent.Semaphore
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

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
        private val root: View? = null,
        private val includedAttributes: Set<ViewAttributesEnum>? = null
) {
    @Suppress("PrivatePropertyName")
    private val RESOURCES_GUARD = Semaphore(1)

    private val viewMap: SparseArray<View> = SparseArray()
    private var serializer: XmlSerializer? = null
    private var tmpXmlName: String? = null

    private fun setAttribute(attrName: ViewAttributesEnum, attrValue: Any?) {
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
                setAttribute(ViewAttributesEnum.ADAPTER_TYPE, adapterItem.javaClass.simpleName)
                isAdapterTypeSet = true
            }
        }
        if (adapterData.isNotEmpty()) {
            setAttribute(ViewAttributesEnum.ADAPTERS, TextUtils.join(",", adapterData))
        }
    }

    private fun isAttributeIncluded(attr: ViewAttributesEnum): Boolean
        = null == includedAttributes || includedAttributes.contains(attr)

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
                ViewAttributesEnum.INDEX to { viewElement.index },
                ViewAttributesEnum.PACKAGE to { viewElement.packageName },
                ViewAttributesEnum.CLASS to { className },
                ViewAttributesEnum.CONTENT_DESC to { viewElement.contentDescription },
                ViewAttributesEnum.CHECKABLE to { viewElement.isCheckable },
                ViewAttributesEnum.CHECKED to { viewElement.isChecked },
                ViewAttributesEnum.CLICKABLE to { viewElement.isClickable },
                ViewAttributesEnum.ENABLED to { viewElement.isEnabled },
                ViewAttributesEnum.FOCUSABLE to { viewElement.isFocusable },
                ViewAttributesEnum.FOCUSED to { viewElement.isFocused },
                ViewAttributesEnum.SCROLLABLE to { viewElement.isScrollable },
                ViewAttributesEnum.LONG_CLICKABLE to { viewElement.isLongClickable },
                ViewAttributesEnum.PASSWORD to { viewElement.isPassword },
                ViewAttributesEnum.SELECTED to { viewElement.isSelected },
                ViewAttributesEnum.VISIBLE to { viewElement.isVisible },
                ViewAttributesEnum.BOUNDS to { viewElement.bounds.toShortString() },
                ViewAttributesEnum.TEXT to null,
                ViewAttributesEnum.HINT to null,
                ViewAttributesEnum.RESOURCE_ID to { viewElement.resourceId },
                ViewAttributesEnum.VIEW_TAG to { viewElement.viewTag },
                ViewAttributesEnum.ADAPTERS to null,
                ViewAttributesEnum.ADAPTER_TYPE to null
        ).forEach {
            when (it.key) {
                ViewAttributesEnum.TEXT, ViewAttributesEnum.HINT ->
                    if (!isTextOrHintRecorded && isAttributeIncluded(it.key)) {
                        viewElement.text?.let { text ->
                            setAttribute(ViewAttributesEnum.TEXT, text.rawText)
                            setAttribute(ViewAttributesEnum.HINT, text.isHint)
                            isTextOrHintRecorded = true
                        }
                    }
                ViewAttributesEnum.ADAPTERS, ViewAttributesEnum.ADAPTER_TYPE ->
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
            AndroidLogger.warn("Skipping traversal of ${view.javaClass.name}'s children, since " +
                    "the current depth has reached its maximum allowed value of $depth")
        }

        serializer?.endTag(NAMESPACE, tagName)
    }

    private fun toStream(): InputStream {
        var lastError: Throwable? = null
        val rootView = root ?: ViewGetter().rootView
        // Try to serialize the xml into the memory first, since it is fast
        // Switch to a file system serializer if the first approach causes OutOfMemory
        for (streamType in arrayOf<Class<*>>(ByteArrayOutputStream::class.java, FileOutputStream::class.java)) {
            serializer = Xml.newSerializer()
            viewMap.clear()

            try {
                val outputStream = if (streamType == FileOutputStream::class.java) {
                    tmpXmlName = "${UUID.randomUUID()}.xml"
                    getApplicationContext<Context>().openFileOutput(tmpXmlName, Context.MODE_PRIVATE)
                } else ByteArrayOutputStream()
                try {
                    serializer?.let {
                        it.setOutput(outputStream, XML_ENCODING)
                        it.startDocument(XML_ENCODING, true)
                        it.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                        val startTime = SystemClock.uptimeMillis()
                        serializeView(rootView, 0)
                        it.endDocument()
                        AndroidLogger.info("The source XML tree has been fetched in " +
                                "${SystemClock.uptimeMillis() - startTime}ms " +
                                "using ${streamType.simpleName}")
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

    fun findViewsByXPath(xpathSelector: String): List<View> {
        val expr = try {
            XPATH.compile(xpathSelector)
        } catch (xe: XPathExpressionException) {
            throw XPathLookupException(xpathSelector, xe.message!!)
        }
        return RESOURCES_GUARD.withPermit({
            toStream().use { xmlStream ->
                val list = expr.evaluate(InputSource(xmlStream), XPathConstants.NODESET) as NodeList
                (0 until list.length).map { index ->
                    viewMap.get(Integer.parseInt((list.item(index) as Element).getAttribute(VIEW_INDEX)))
                }
            }
        }, { performCleanup() })
    }
}
