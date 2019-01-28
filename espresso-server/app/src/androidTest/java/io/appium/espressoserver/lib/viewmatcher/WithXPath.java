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

package io.appium.espressoserver.lib.viewmatcher;

import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

import androidx.annotation.Nullable;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.SourceDocument;

public class WithXPath {
    public static Matcher<View> withXPath(@Nullable final View root, final String xpath,
                                          @Nullable final Integer index) throws AppiumException {
        // Get a list of the Views that match the provided xpath
        final List<View> matchedXPathViews = new SourceDocument(root).findViewsByXPath(xpath);

        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (index != null) {
                    // If index is not null, match it with the xpath in the list at the provided index
                    return index < matchedXPathViews.size() && matchedXPathViews.get(index).equals(item);
                }

                // If index is null, then we only check that the view is contained in the list of matched xpaths
                return matchedXPathViews.contains(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Looked for element with XPath %s", xpath));
            }
        };
    }

    public static Matcher<View> withXPath(@Nullable final View root, final String xpath) throws AppiumException {
        return withXPath(root, xpath, null);
    }
}
