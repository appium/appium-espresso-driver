package io.appium.espressoserver.lib.model;

import android.view.View;
import android.view.ViewGroup;

import org.w3c.dom.*;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.HashMap;
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
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException;
import io.appium.espressoserver.lib.viewaction.RootViewFinder;

public class SourceDocument {

    private Document doc;
    private final Map<Element, View> viewMap = new HashMap<>();
    private boolean cacheElementReferences;
    private final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private Transformer transformer;

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
        View rootView = (new RootViewFinder()).getRootView();
        buildXML(doc, null, rootView);
    }

    /**
     * Recursively visit all of the views and map them to XML elements
     * @param doc XML Document
     * @param parentElement Element that this new element will be appended to
     * @param view Android View that will map to an Element
     */
    private void buildXML(Document doc, Element parentElement, View view) {
        // TODO: Investigate if this is safe and if we need to strip out characters before constructing XML document
        Element element = doc.createElement(view.getClass().getName());

        // Set attributes
        ViewElement viewElement = new ViewElement(view);
        element.setAttribute(ViewAttributesEnum.CONTENT_DESC.getName(), viewElement.getContentDescription().toString());
        element.setAttribute(ViewAttributesEnum.BOUNDS.getName(), viewElement.getBounds().toShortString());
        element.setAttribute(ViewAttributesEnum.FOCUSED.getName(), Boolean.toString(viewElement.isFocused()));
        element.setAttribute(ViewAttributesEnum.CLICKABLE.getName(), Boolean.toString(viewElement.isClickable()));
        element.setAttribute(ViewAttributesEnum.LONG_CLICKABLE.getName(), Boolean.toString(viewElement.isLongClickable()));
        element.setAttribute(ViewAttributesEnum.CLASS.getName(), viewElement.getClassName());
        element.setAttribute(ViewAttributesEnum.INDEX.getName(), Integer.toString(viewElement.getIndex()));

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

    @Nullable
    public static View findViewByXPath(String xpathSelector) throws XPathLookupException {
        try {
            SourceDocument sourceDocument = new SourceDocument(true);
            XPath xpath = XPathFactory.newInstance().newXPath();
            Element elementNode = (Element) xpath.evaluate(xpathSelector, sourceDocument.doc, XPathConstants.NODE);
            if (elementNode == null) {
                return null;
            }
            return sourceDocument.viewMap.get(elementNode);
        } catch (ParserConfigurationException pe) {
            throw new XPathLookupException(xpathSelector, pe.getMessage());
        } catch (XPathExpressionException xe) {
            throw new XPathLookupException(xpathSelector, xe.getMessage());
        } catch (TransformerException te) {
            throw new XPathLookupException(xpathSelector, te.getMessage());
        }
    }
}
