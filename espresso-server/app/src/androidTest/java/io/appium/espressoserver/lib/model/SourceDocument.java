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

import android.view.View;
import android.view.ViewGroup;

import org.apache.xml.utils.XMLChar;
import org.w3c.dom.*;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class SourceDocument {

    private Document doc;
    private final Map<Element, View> viewMap = new HashMap<>();
    private boolean cacheElementReferences;
    private final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private Transformer transformer;
    private static XPath xpath = XPathFactory.newInstance().newXPath();

    public SourceDocument() throws ParserConfigurationException, TransformerException {
        init();
        this.cacheElementReferences = false;
        buildXML();
    }

    private SourceDocument(boolean cacheElementReferences) throws ParserConfigurationException, TransformerException {
        init();
        this.cacheElementReferences = cacheElementReferences;
        buildXML();
    }

    private void init() throws TransformerException {
        transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    }

    /**
     * Recursively visit all of the views and map them to XML elements
     */
    private void buildXML() {
        // Create an empty document
        doc = docBuilder.newDocument();

        // Get reference to root view
        View rootView = (new ViewFinder()).getRootView();
        buildXML(doc, null, rootView);
    }

    /**
     * Recursively visit all of the views and map them to XML elements
     *
     * @param doc           XML Document
     * @param parentElement Element that this new element will be appended to
     * @param view          Android View that will map to an Element
     */
    private void buildXML(Document doc, Element parentElement, View view) {
        ViewElement viewElement = new ViewElement(view);
        Element element = doc.createElement(viewElement.getClassName());

        // Set attributes
        setAttribute(element, ViewAttributesEnum.INDEX, Integer.toString(viewElement.getIndex()));
        setAttribute(element, ViewAttributesEnum.PACKAGE, viewElement.getPackageName());
        setAttribute(element, ViewAttributesEnum.TEXT, viewElement.getText());
        setAttribute(element, ViewAttributesEnum.CLASS, viewElement.getClassName());
        setAttribute(element, ViewAttributesEnum.CONTENT_DESC, viewElement.getContentDescription());
        setAttribute(element, ViewAttributesEnum.CHECKABLE, Boolean.toString(viewElement.isCheckable()));
        setAttribute(element, ViewAttributesEnum.CHECKED, Boolean.toString(viewElement.isChecked()));
        setAttribute(element, ViewAttributesEnum.CLICKABLE, Boolean.toString(viewElement.isClickable()));
        setAttribute(element, ViewAttributesEnum.ENABLED, Boolean.toString(viewElement.isEnabled()));
        setAttribute(element, ViewAttributesEnum.FOCUSABLE, Boolean.toString(viewElement.isFocusable()));
        setAttribute(element, ViewAttributesEnum.FOCUSED, Boolean.toString(viewElement.isFocused()));
        setAttribute(element, ViewAttributesEnum.SCROLLABLE, Boolean.toString(viewElement.isScrollable()));
        setAttribute(element, ViewAttributesEnum.LONG_CLICKABLE, Boolean.toString(viewElement.isLongClickable()));
        setAttribute(element, ViewAttributesEnum.PASSWORD, Boolean.toString(viewElement.isPassword()));
        setAttribute(element, ViewAttributesEnum.SELECTED, Boolean.toString(viewElement.isSelected()));
        setAttribute(element, ViewAttributesEnum.BOUNDS, viewElement.getBounds().toShortString());
        setAttribute(element, ViewAttributesEnum.TEXT, viewElement.getText());
        setAttribute(element, ViewAttributesEnum.RESOURCE_ID, viewElement.getResourceId());

        // If this is the rootElement, append it to the document
        if (parentElement == null) {
            doc.appendChild(element);
        } else {
            parentElement.appendChild(element);
        }

        // If cacheElementReferences == true, then cache a reference to the View
        if (cacheElementReferences) {
            viewMap.put(element, view);
        }

        // Visit the children and build them too
        try {
            for (int index = 0; index < ((ViewGroup) view).getChildCount(); ++index) {
                View childView = ((ViewGroup) view).getChildAt(index);
                buildXML(doc, element, childView);
            }
        } catch (ClassCastException e) {
            // If it couldn't be cast to a ViewGroup, it has no children
        }
    }

    public String toXMLString() throws TransformerException {
        DOMSource source = new DOMSource(doc);
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);
        return stringWriter.toString();
    }

    public static List<View> findViewsByXPath(String xpathSelector) throws XPathLookupException {
        try {
            // Get the Nodes that match the provided xpath
            SourceDocument sourceDocument = new SourceDocument(true);
            XPathExpression expr = xpath.compile(xpathSelector);
            NodeList list = (NodeList) expr.evaluate(sourceDocument.doc, XPathConstants.NODESET);

            // Get a list of elements that are associated with that node
            List<View> views = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                views.add(sourceDocument.viewMap.get(element));
            }
            return views;
        } catch (ParserConfigurationException pe) {
            throw new XPathLookupException(xpathSelector, pe.getMessage());
        } catch (XPathExpressionException xe) {
            throw new XPathLookupException(xpathSelector, xe.getMessage());
        } catch (TransformerException te) {
            throw new XPathLookupException(xpathSelector, te.getMessage());
        }
    }

    // Original Google code here broke UTF characters
    private static String stripInvalidXMLChars(CharSequence charSequence) {
        final StringBuilder sb = new StringBuilder(charSequence.length());
        for (int i = 0; i < charSequence.length(); i++) {
            char c = charSequence.charAt(i);
            if (XMLChar.isValid(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static void setAttribute(Element element, ViewAttributesEnum viewAttributesEnum,
                                     @Nullable CharSequence attrValue) {
        if (attrValue == null) {
            // Do not write attributes, whose values equal to null
            return;
        }
        element.setAttribute(stripInvalidXMLChars(viewAttributesEnum.toString()),
                stripInvalidXMLChars(attrValue));
    }
}
