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
import android.widget.TextView;

import org.hamcrest.Description;

import java.util.regex.Pattern;

import androidx.test.espresso.matcher.BoundedMatcher;

public class RegexpTextMatcher extends BoundedMatcher<View, TextView> {
    public static RegexpTextMatcher withRegexp(Pattern pattern) {
        return new RegexpTextMatcher(pattern);
    }

    private final Pattern pattern;

    private RegexpTextMatcher(Pattern pattern) {
        super(TextView.class);

        this.pattern = pattern;
    }

    @Override
    protected boolean matchesSafely(TextView item) {
        CharSequence text = item.getText();
        if (text == null) {
            return false;
        }
        return pattern.matcher(text).find();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with regexp: ")
                .appendValue(pattern.toString());
    }
}
