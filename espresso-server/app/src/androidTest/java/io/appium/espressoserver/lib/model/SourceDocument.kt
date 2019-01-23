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
import android.widget.Adapter
import android.widget.AdapterView

import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xmlpull.v1.XmlSerializer

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.Semaphore
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException
import io.appium.espressoserver.lib.viewaction.ViewGetter

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import io.appium.espressoserver.lib.helpers.AndroidLogger.logger
import io.appium.espressoserver.lib.helpers.StringHelpers.abbreviate
import io.appium.espressoserver.lib.helpers.XMLHelpers.toNodeName
import io.appium.espressoserver.lib.helpers.XMLHelpers.toSafeString

class SourceDocument @JvmOverloads constructor(private val root: View?, private val viewMap: SparseArray<View>? = SparseArray()) {
    private val RESOURCES_GUARD = Semaphore(1)

    private var serializer: XmlSerializer? = null
    private var tmpXmlName: String? = null

    constructor() : this(null, null) {}

    @Throws(IOException::class)
    private fun setAttribute(attrName: ViewAttributesEnum, attrValue: Any?) {
        // Do not write attributes, whose values equal to null
        if (attrValue != null) {
            // Cut off longer strings to avoid OOM errors
            val xmlValue = abbreviate(toSafeString(attrValue.toString(), NON_XML_CHAR_REPLACEMENT),
                    MAX_XML_VALUE_LENGTH)
            serializer!!.attribute(NAMESPACE, attrName.toString(), xmlValue)
        }
    }

    @Throws(IOException::class)
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
        if (!adapterData.isEmpty()) {
            setAttribute(ViewAttributesEnum.ADAPTERS, TextUtils.join(",", adapterData))
        }
    }

    /**
     * Recursively visit all of the views and map them to XML elements
     *
     * @param view  The root view
     * @param depth The current traversal depth
     */
    @Throws(IOException::class)
    private fun serializeView(view: View?, depth: Int) {
        if (view == null) {
            return
        }

        val viewElement = ViewElement(view)
        val tagName = toXmlNodeName(viewElement.className)
        serializer!!.startTag(NAMESPACE, tagName)

        // Set attributes
        setAttribute(ViewAttributesEnum.INDEX, viewElement.index)
        setAttribute(ViewAttributesEnum.PACKAGE, viewElement.packageName)
        setAttribute(ViewAttributesEnum.CLASS, viewElement.className)
        setAttribute(ViewAttributesEnum.CONTENT_DESC, viewElement.contentDescription)
        setAttribute(ViewAttributesEnum.CHECKABLE, viewElement.isCheckable)
        setAttribute(ViewAttributesEnum.CHECKED, viewElement.isChecked)
        setAttribute(ViewAttributesEnum.CLICKABLE, viewElement.isClickable)
        setAttribute(ViewAttributesEnum.ENABLED, viewElement.isEnabled)
        setAttribute(ViewAttributesEnum.FOCUSABLE, viewElement.isFocusable)
        setAttribute(ViewAttributesEnum.FOCUSED, viewElement.isFocused)
        setAttribute(ViewAttributesEnum.SCROLLABLE, viewElement.isScrollable)
        setAttribute(ViewAttributesEnum.LONG_CLICKABLE, viewElement.isLongClickable)
        setAttribute(ViewAttributesEnum.PASSWORD, viewElement.isPassword)
        setAttribute(ViewAttributesEnum.SELECTED, viewElement.isSelected)
        setAttribute(ViewAttributesEnum.VISIBLE, viewElement.isVisible)
        setAttribute(ViewAttributesEnum.BOUNDS, viewElement.bounds.toShortString())
        val viewText = viewElement.text
        if (viewText != null) {
            setAttribute(ViewAttributesEnum.TEXT, viewText.rawText)
            setAttribute(ViewAttributesEnum.HINT, viewText.isHint)
        }
        setAttribute(ViewAttributesEnum.RESOURCE_ID, viewElement.resourceId)
        setAttribute(ViewAttributesEnum.VIEW_TAG, viewElement.viewTag)
        if (view is AdapterView<*>) {
            recordAdapterViewInfo((view as AdapterView<*>?)!!)
        }

        if (viewMap != null) {
            serializer!!.attribute(NAMESPACE, VIEW_INDEX, Integer.toString(viewMap.size()))
            viewMap.put(viewMap.size(), view)
        }

        if (depth < MAX_TRAVERSAL_DEPTH) {
            // Visit the children and build them too
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) {
                    serializeView(view.getChildAt(index), depth + 1)
                }
            }
        } else {
            logger.warn(String.format("Skipping traversal of %s's children, since the current depth " + "has reached its maximum allowed value of %s", view.javaClass.name, depth))
        }

        serializer!!.endTag(NAMESPACE, tagName)
    }

    @Throws(AppiumException::class)
    private fun toStream(): InputStream {
        var lastError: Throwable? = null
        val rootView = root ?: ViewGetter().rootView
        // Try to serialize the xml into the memory first, since it is fast
        // Switch to a file system serializer if the first approach causes OutOfMemory
        for (streamType in arrayOf<Class<*>>(ByteArrayOutputStream::class.java, FileOutputStream::class.java)) {
            serializer = Xml.newSerializer()
            viewMap?.clear()

            try {
                val outputStream: OutputStream
                if (streamType == FileOutputStream::class.java) {
                    tmpXmlName = String.format("%s.xml", UUID.randomUUID().toString())
                    outputStream = getApplicationContext<Context>().openFileOutput(tmpXmlName, Context.MODE_PRIVATE)
                } else {
                    outputStream = ByteArrayOutputStream()
                }
                try {
                    serializer!!.setOutput(outputStream, XML_ENCODING)
                    serializer!!.startDocument(XML_ENCODING, true)
                    serializer!!.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                    val startTime = SystemClock.uptimeMillis()
                    serializeView(rootView, 0)
                    serializer!!.endDocument()
                    logger.info(String.format("The source XML tree has been fetched in %sms using %s",
                            SystemClock.uptimeMillis() - startTime, streamType.simpleName))
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
        throw AppiumException(lastError)
    }

    private fun performCleanup() {
        if (tmpXmlName != null) {
            getApplicationContext<Context>().deleteFile(tmpXmlName)
            tmpXmlName = null
        }
    }

    @Throws(AppiumException::class)
    fun toXMLString(): String {
        try {
            RESOURCES_GUARD.acquire()
        } catch (e: InterruptedException) {
            throw AppiumException(e)
        }

        try {
            toStream().use { xmlStream ->
                val sb = StringBuilder()
                val reader = BufferedReader(InputStreamReader(xmlStream, XML_ENCODING))
                var line: String? = reader.readLine()
                while (line != null) {
                    sb.append(line)
                    line = reader.readLine()
                }
                return sb.toString()
            }
        } catch (e: IOException) {
            throw AppiumException(e)
        } finally {
            performCleanup()
            RESOURCES_GUARD.release()
        }
    }

    @Throws(AppiumException::class)
    fun findViewsByXPath(xpathSelector: String): List<View> {
        var list: NodeList? = null
        try {
            // Get the Nodes that match the provided xpath
            val expr = xpath.compile(xpathSelector)
            try {
                RESOURCES_GUARD.acquire()
            } catch (e: InterruptedException) {
                throw AppiumException(e)
            }

            try {
                toStream().use { xmlStream -> list = expr.evaluate(InputSource(xmlStream), XPathConstants.NODESET) as NodeList }
            } catch (e: IOException) {
                throw AppiumException(e)
            } finally {
                performCleanup()
                RESOURCES_GUARD.release()
            }

            // Get a list of elements that are associated with that node
            val views = ArrayList<View>()
            if (list != null) {
                for (i in 0 until list!!.length) {
                    val element = list!!.item(i) as Element

                    views.add(viewMap!!.get(Integer.parseInt(element.getAttribute(VIEW_INDEX))))
                }
            }
            return views
        } catch (xe: XPathExpressionException) {
            throw XPathLookupException(xpathSelector, xe.message)
        }

    }

    companion object {
        private val xpath = XPathFactory.newInstance().newXPath()
        private val NON_XML_CHAR_REPLACEMENT = "?"
        private val VIEW_INDEX = "viewIndex"
        private val NAMESPACE = ""
        private val DEFAULT_VIEW_CLASS_NAME = "android.view.View"
        private var MAX_TRAVERSAL_DEPTH = 70
        private val MAX_XML_VALUE_LENGTH = 64 * 1024
        private val XML_ENCODING = "UTF-8"

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
                logger.info(String.format("Rewrote class name '%s' to XML node name '%s'", className, fixedName))
            }
            return fixedName
        }

        fun `$setMaxTraverseDepth`(maxTraverseDepth: Int) {
            SourceDocument.MAX_TRAVERSAL_DEPTH = maxTraverseDepth
        }
    }
}
