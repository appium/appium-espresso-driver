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

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.Xml;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException;
import io.appium.espressoserver.lib.viewaction.ViewGetter;

import static androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal;
import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;
import static io.appium.espressoserver.lib.helpers.XMLHelpers.toNodeName;
import static io.appium.espressoserver.lib.helpers.XMLHelpers.toSafeString;

public class SourceDocument {
    private static XPath xpath = XPathFactory.newInstance().newXPath();
    private static final String NON_XML_CHAR_REPLACEMENT = "?";
    private static final String VIEW_INDEX = "viewIndex";
    private static final String NAMESPACE = "";
    private final static String DEFAULT_VIEW_CLASS_NAME = "android.view.View";

    private XmlSerializer serializer;
    @Nullable
    private final SparseArray<View> viewMap;
    @Nullable
    private final View root;

    public SourceDocument() {
        this(null, null);
    }

    private SourceDocument(@Nullable View root, @Nullable SparseArray<View> viewMap) {
        this.root = root;
        this.viewMap = viewMap;
    }

    private void setAttribute(ViewAttributesEnum attrName, @Nullable Object attrValue) throws IOException {
        // Do not write attributes, whose values equal to null
        if (attrValue != null) {
            serializer.attribute(NAMESPACE, attrName.toString(),
                    toSafeString(String.valueOf(attrValue), NON_XML_CHAR_REPLACEMENT));
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
     * @param view The root view
     */
    private void serializeView(View view) throws IOException {
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

        // Visit the children and build them too
        for (View childView : breadthFirstViewTraversal(view)) {
            if (childView != view) {
                serializeView(childView);
            }
        }

        serializer.endTag(NAMESPACE, tagName);
    }

    public synchronized String toXMLString() throws AppiumException {
        serializer = Xml.newSerializer();
        if (viewMap != null) {
            viewMap.clear();
        }
        final StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            final long startTime = SystemClock.uptimeMillis();
            serializeView(root == null ? new ViewGetter().getRootView() : root);
            serializer.endDocument();
            logger.info(String.format("The source XML tree has been fetched in %sms", SystemClock.uptimeMillis() - startTime));
            return writer.toString();
        } catch (Exception e) {
            throw new AppiumException(e);
        }
    }

    public static List<View> findViewsByXPath(@Nullable View root, String xpathSelector) throws AppiumException {
        final SparseArray<View> viewMap = new SparseArray<>();
        try {
            // Get the Nodes that match the provided xpath
            XPathExpression expr = xpath.compile(xpathSelector);
            NodeList list = (NodeList) expr.evaluate(
                    new InputSource(new StringReader(new SourceDocument(root, viewMap).toXMLString())),
                    XPathConstants.NODESET);

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
}
