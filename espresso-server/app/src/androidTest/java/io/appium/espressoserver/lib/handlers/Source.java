package io.appium.espressoserver.lib.handlers;

import android.view.View;
import android.view.ViewGroup;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.ViewAttributesEnum;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.RootViewFinder;

public class Source implements RequestHandler<AppiumParams, String> {

    @Override
    @Nullable
    public String handle(AppiumParams params) throws AppiumException {

        try {
            // Build a document
            Document doc;
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();

            // Populate the XML
            View rootView = (new RootViewFinder()).getRootView();
            buildXML(doc, null, rootView);

            // Write the XML to string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);
            return stringWriter.toString();

        } catch (ParserConfigurationException pe) {
            throw new AppiumException(String.format("Could not parse XML from source: %s", pe.getMessage()));
        } catch (TransformerConfigurationException te) {
            throw new AppiumException(String.format("Could not parse XML from source: %s", te.getMessage()));
        } catch (TransformerException te) {
            throw new AppiumException(String.format("Could not parse XML from source: %s", te.getMessage()));
        }
    }

    /**
     * Recursively visit all of the views and map them to XML elements
     * @param doc XML Document
     * @param parentElement Element that this new element will be appended to
     * @param view Android View that will map to an Element
     */
    public void buildXML(Document doc, Element parentElement, View view) {
        // TODO: Extract this logic out to it's own class so it can be re-used with XPath locators
        Element element = doc.createElement(view.getClass().getName());

        // Set attributes
        ViewElement viewElement = new ViewElement(view);
        element.setAttribute(ViewAttributesEnum.CONTENT_DESC.getName(), viewElement.getContentDescription());
        element.setAttribute(ViewAttributesEnum.BOUNDS.getName(), viewElement.getBounds());
        element.setAttribute(ViewAttributesEnum.FOCUSED.getName(), viewElement.getFocused());
        element.setAttribute(ViewAttributesEnum.CLICKABLE.getName(), viewElement.getClickable());
        element.setAttribute(ViewAttributesEnum.LONG_CLICKABLE.getName(), viewElement.getLongClickable());
        element.setAttribute(ViewAttributesEnum.CLASS.getName(), viewElement.getClassName());
        element.setAttribute(ViewAttributesEnum.INDEX.getName(), viewElement.getIndex());

        // If this is the rootElement, append it to the document
        if (parentElement == null) {
            doc.appendChild(element);
        } else {
            parentElement.appendChild(element);
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
}