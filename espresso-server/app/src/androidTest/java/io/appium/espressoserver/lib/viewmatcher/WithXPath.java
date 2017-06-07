package io.appium.espressoserver.lib.viewmatcher;

import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException;
import io.appium.espressoserver.lib.model.SourceDocument;

public class WithXPath {
    public static Matcher<View> withXPath(final String xpath) throws XPathLookupException {
        final View matchedXPathView = SourceDocument.findViewByXPath(xpath);

        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                return item.equals(matchedXPathView);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Looked for element with XPath %s", xpath));
            }
        };
    }
}
