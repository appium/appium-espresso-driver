package io.appium.espressoserver.test.model;

import android.content.Context;
import android.widget.LinearLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.SourceDocument;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SourceDocumentTest {

    @Test
    public void shouldHandleLargeMemoryHierarchies() throws AppiumException {
        Context context = getApplicationContext();
        LinearLayout listView = new LinearLayout(context);
        LinearLayout currView = listView;

        for (int i=0; i<50; i++) {
            LinearLayout nextView = new LinearLayout(context);
            currView.addView(nextView);
            currView = nextView;
        }

        // Set the max traverse depth low. JVM runs out of heap space quicker than Android.
        SourceDocument.Companion.$setMaxTraverseDepth(3);
        SourceDocument sourceDoc = new SourceDocument(listView);
        String xml = sourceDoc.toXMLString();
        assertTrue(xml.startsWith("<?xml"));
    }
}
