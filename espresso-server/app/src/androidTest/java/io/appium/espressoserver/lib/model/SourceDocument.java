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

package io.appium.espressoserver.lib.model;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException;
import io.appium.espressoserver.lib.viewaction.ViewGetter;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;
import static io.appium.espressoserver.lib.helpers.StringHelpers.abbreviate;
import static io.appium.espressoserver.lib.helpers.XMLHelpers.toNodeName;
import static io.appium.espressoserver.lib.helpers.XMLHelpers.toSafeString;

public class SourceDocument {
    private static XPath xpath = XPathFactory.newInstance().newXPath();
    private static final String NON_XML_CHAR_REPLACEMENT = "?";
    private static final String VIEW_INDEX = "viewIndex";
    private static final String NAMESPACE = "";
    private static final String DEFAULT_VIEW_CLASS_NAME = "android.view.View";
    private static int MAX_TRAVERSAL_DEPTH = 70;
    private static final int MAX_XML_VALUE_LENGTH = 64 * 1024;
    private static final String XML_ENCODING = "UTF-8";
    private final Semaphore RESOURCES_GUARD = new Semaphore(1);

    private XmlSerializer serializer;
    @Nullable
    private final SparseArray<View> viewMap;
    @Nullable
    private final View root;
    private String tmpXmlName;

    public SourceDocument() {
        this(null, null);
    }

    public SourceDocument(@Nullable View root) {
        this(root, new SparseArray<View>());
    }

    public SourceDocument(@Nullable View root, @Nullable SparseArray<View> viewMap) {
        this.root = root;
        this.viewMap = viewMap;
    }

    private void setAttribute(ViewAttributesEnum attrName, @Nullable Object attrValue) throws IOException {
        // Do not write attributes, whose values equal to null
        if (attrValue != null) {
            // Cut off longer strings to avoid OOM errors
            String xmlValue = abbreviate(toSafeString(String.valueOf(attrValue), NON_XML_CHAR_REPLACEMENT),
                    MAX_XML_VALUE_LENGTH);
            serializer.attribute(NAMESPACE, attrName.toString(), xmlValue);
        }
    }

    private void recordAdapterViewInfo(AdapterView adapterView) throws IOException {
        Adapter adapter = adapterView.getAdapter();
        if (adapter == null) {
            return;
        }
        int adapterCount = adapter.getCount();
        List<String> adapterData = new ArrayList<>();
        boolean isAdapterTypeSet = false;
        for (int i = 0; i < adapterCount; i++) {
            Object adapterItem = adapter.getItem(i);
            if (adapterItem == null) {
                continue;
            }
            adapterData.add(adapterItem.toString());

            // Get the type of the adapter item
            if (!isAdapterTypeSet) {
                setAttribute(ViewAttributesEnum.ADAPTER_TYPE, adapterItem.getClass().getSimpleName());
                isAdapterTypeSet = true;
            }
        }
        if (!adapterData.isEmpty()) {
            setAttribute(ViewAttributesEnum.ADAPTERS, TextUtils.join(",", adapterData));
        }
    }

    private static String toXmlNodeName(@Nullable String className) {
        if (className == null || className.trim().isEmpty()) {
            return DEFAULT_VIEW_CLASS_NAME;
        }

        String fixedName = className
                .replaceAll("[$@#&]", ".")
                .replaceAll("\\.+", ".")
                .replaceAll("(^\\.|\\.$)", "");
        fixedName = toNodeName(fixedName);
        if (fixedName.trim().isEmpty()) {
            fixedName = DEFAULT_VIEW_CLASS_NAME;
        }
        if (!fixedName.equals(className)) {
            logger.info(String.format("Rewrote class name '%s' to XML node name '%s'", className, fixedName));
        }
        return fixedName;
    }

    /**
     * Recursively visit all of the views and map them to XML elements
     *
     * @param view  The root view
     * @param depth The current traversal depth
     */
    private void serializeView(View view, final int depth) throws IOException {
        if (view == null) {
            return;
        }

        ViewElement viewElement = new ViewElement(view);
        final String tagName = toXmlNodeName(viewElement.getClassName());
        serializer.startTag(NAMESPACE, tagName);

        // Set attributes
        setAttribute(ViewAttributesEnum.INDEX, viewElement.getIndex());
        setAttribute(ViewAttributesEnum.PACKAGE, viewElement.getPackageName());
        setAttribute(ViewAttributesEnum.CLASS, viewElement.getClassName());
        setAttribute(ViewAttributesEnum.CONTENT_DESC, viewElement.getContentDescription());
        setAttribute(ViewAttributesEnum.CHECKABLE, viewElement.isCheckable());
        setAttribute(ViewAttributesEnum.CHECKED, viewElement.isChecked());
        setAttribute(ViewAttributesEnum.CLICKABLE, viewElement.isClickable());
        setAttribute(ViewAttributesEnum.ENABLED, viewElement.isEnabled());
        setAttribute(ViewAttributesEnum.FOCUSABLE, viewElement.isFocusable());
        setAttribute(ViewAttributesEnum.FOCUSED, viewElement.isFocused());
        setAttribute(ViewAttributesEnum.SCROLLABLE, viewElement.isScrollable());
        setAttribute(ViewAttributesEnum.LONG_CLICKABLE, viewElement.isLongClickable());
        setAttribute(ViewAttributesEnum.PASSWORD, viewElement.isPassword());
        setAttribute(ViewAttributesEnum.SELECTED, viewElement.isSelected());
        setAttribute(ViewAttributesEnum.VISIBLE, viewElement.isVisible());
        setAttribute(ViewAttributesEnum.BOUNDS, viewElement.getBounds().toShortString());
        final ViewText viewText = viewElement.getText();
        if (viewText != null) {
            setAttribute(ViewAttributesEnum.TEXT, viewText.getRawText());
            setAttribute(ViewAttributesEnum.HINT, viewText.isHint());
        }
        setAttribute(ViewAttributesEnum.RESOURCE_ID, viewElement.getResourceId());
        setAttribute(ViewAttributesEnum.VIEW_TAG, viewElement.getViewTag());
        if (view instanceof AdapterView) {
            recordAdapterViewInfo((AdapterView) view);
        }

        if (viewMap != null) {
            serializer.attribute(NAMESPACE, VIEW_INDEX, Integer.toString(viewMap.size()));
            viewMap.put(viewMap.size(), view);
        }

        if (depth < MAX_TRAVERSAL_DEPTH) {
            // Visit the children and build them too
            if (view instanceof ViewGroup) {
                for (int index = 0; index < ((ViewGroup) view).getChildCount(); ++index) {
                    serializeView(((ViewGroup) view).getChildAt(index), depth + 1);
                }
            }
        } else {
            logger.warn(String.format("Skipping traversal of %s's children, since the current depth " +
                    "has reached its maximum allowed value of %s", view.getClass().getName(), depth));
        }

        serializer.endTag(NAMESPACE, tagName);
    }

    private InputStream toStream() throws AppiumException {
        Throwable lastError = null;
        final View rootView = root == null ? new ViewGetter().getRootView() : root;
        // Try to serialize the xml into the memory first, since it is fast
        // Switch to a file system serializer if the first approach causes OutOfMemory
        for (Class<?> streamType : new Class[]{ByteArrayOutputStream.class, FileOutputStream.class}) {
            serializer = Xml.newSerializer();
            if (viewMap != null) {
                viewMap.clear();
            }

            try {
                OutputStream outputStream;
                if (streamType.equals(FileOutputStream.class)) {
                    tmpXmlName = String.format("%s.xml", UUID.randomUUID().toString());
                    outputStream = getApplicationContext().openFileOutput(tmpXmlName, Context.MODE_PRIVATE);
                } else {
                    outputStream = new ByteArrayOutputStream();
                }
                try {
                    serializer.setOutput(outputStream, XML_ENCODING);
                    serializer.startDocument(XML_ENCODING, true);
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    final long startTime = SystemClock.uptimeMillis();
                    serializeView(rootView, 0);
                    serializer.endDocument();
                    logger.info(String.format("The source XML tree has been fetched in %sms using %s",
                            SystemClock.uptimeMillis() - startTime, streamType.getSimpleName()));
                } catch (OutOfMemoryError e) {
                    lastError = e;
                    continue;
                } finally {
                    outputStream.close();
                }
                return outputStream instanceof FileOutputStream
                        ? getApplicationContext().openFileInput(tmpXmlName)
                        : new ByteArrayInputStream(((ByteArrayOutputStream) outputStream).toByteArray());
            } catch (IOException e) {
                lastError = e;
            }
        }
        if (lastError instanceof OutOfMemoryError) {
            throw (OutOfMemoryError) lastError;
        }
        throw new AppiumException(lastError);
    }

    private void performCleanup() {
        if (tmpXmlName != null) {
            getApplicationContext().deleteFile(tmpXmlName);
            tmpXmlName = null;
        }
    }

    public String toXMLString() throws AppiumException {
        try {
            RESOURCES_GUARD.acquire();
        } catch (InterruptedException e) {
            throw new AppiumException(e);
        }
        try (InputStream xmlStream = toStream()) {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(xmlStream, XML_ENCODING));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new AppiumException(e);
        } finally {
            performCleanup();
            RESOURCES_GUARD.release();
        }
    }

    public List<View> findViewsByXPath(String xpathSelector) throws AppiumException {
        try {
            // Get the Nodes that match the provided xpath
            XPathExpression expr = xpath.compile(xpathSelector);
            NodeList list;
            try {
                RESOURCES_GUARD.acquire();
            } catch (InterruptedException e) {
                throw new AppiumException(e);
            }
            try (InputStream xmlStream = toStream()) {
                list = (NodeList) expr.evaluate(new InputSource(xmlStream), XPathConstants.NODESET);
            } catch (IOException e) {
                throw new AppiumException(e);
            } finally {
                performCleanup();
                RESOURCES_GUARD.release();
            }

            // Get a list of elements that are associated with that node
            List<View> views = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                //noinspection ConstantConditions
                views.add(viewMap.get(Integer.parseInt(element.getAttribute(VIEW_INDEX))));
            }
            return views;
        } catch (XPathExpressionException xe) {
            throw new XPathLookupException(xpathSelector, xe.getMessage());
        }
    }

    public static void $setMaxTraverseDepth(int maxTraverseDepth) {
        SourceDocument.MAX_TRAVERSAL_DEPTH = maxTraverseDepth;
    }
}
