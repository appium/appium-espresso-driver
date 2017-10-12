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

package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class ViewTextGetter {

    private final View[] views = {null};

    /**
     * Hack way of getting a view. Similar to 'ViewFinder' class.
     */
    private class GetTextViewAction implements ViewAction {
        @Override
        public Matcher<View> getConstraints() {
            // This is a hack constraint that passes any view through
            return isDescendantOfA(isRoot());
        }

        @Override
        public String getDescription() {
            return "getting a view reference";
        }

        @Override
        public void perform(UiController uiController, View view) {
            views[0] = view;
        }
    }

    public CharSequence get(ViewInteraction viewInteraction) throws AppiumException {
        try {
            viewInteraction.perform(new GetTextViewAction());
            TextView textView = (TextView) views[0];
            return textView.getText();
        } catch (ClassCastException cce) {
            throw new AppiumException(String.format("Views of class type %s have no text property",
                    views.getClass().getName()));
        }
    }
}
