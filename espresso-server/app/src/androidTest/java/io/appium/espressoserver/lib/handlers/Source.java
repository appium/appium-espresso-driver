package io.appium.espressoserver.lib.handlers;


import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.SourceDocument;

public class Source implements RequestHandler<AppiumParams, String> {

    @Override
    @Nullable
    public String handle(AppiumParams params) throws AppiumException {

        try {
            return new SourceDocument().toXMLString();
        } catch (ParserConfigurationException pe) {
            throw new AppiumException(String.format("Could not parse XML from source: %s", pe.getMessage()));
        } catch (TransformerConfigurationException te) {
            throw new AppiumException(String.format("Could not parse XML from source: %s", te.getMessage()));
        } catch (TransformerException te) {
            throw new AppiumException(String.format("Could not parse XML from source: %s", te.getMessage()));
        }
    }
}