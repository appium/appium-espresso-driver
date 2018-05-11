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

import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

public class ViewTextGetter {
    public CharSequence get(ViewInteraction viewInteraction) throws AppiumException {
        final View view = new ViewFinder().getView(viewInteraction);
        if (view instanceof ProgressBar) {
            return Integer.toString(((ProgressBar) view).getProgress());
        }
        if (view instanceof NumberPicker) {
            return Integer.toString(((NumberPicker) view).getValue());
        }
        if (view instanceof EditText) {
            return ((EditText) view).getText();
        }
        if (view instanceof TextView) {
            return ((TextView) view).getText();
        }
        throw new AppiumException(String.format("Views of class type %s have no text property",
                view.getClass().getName()));
    }
}
