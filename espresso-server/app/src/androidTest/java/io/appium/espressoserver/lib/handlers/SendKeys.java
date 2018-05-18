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

package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.TextParams;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

import static android.support.test.espresso.action.ViewActions.typeText;

public class SendKeys implements RequestHandler<TextParams, Void> {

    @Override
    public Void handle(TextParams params) throws AppiumException {
        String id = params.getElementId();
        ViewInteraction viewInteraction = Element.getViewInteractionById(id);
        View view = Element.getViewById(id);

        // Convert the array of text to a String
        String[] textArray = params.getValue();
        StringBuilder stringBuilder = new StringBuilder();
        for (String text : textArray) {
            stringBuilder.append(text);
        }

        String value = stringBuilder.toString();

        try {
            if (view instanceof ProgressBar) {
                ((ProgressBar) view).setProgress(Integer.parseInt(value));
                return null;
            }
            if (view instanceof NumberPicker) {
                ((NumberPicker) view).setValue(Integer.parseInt(value));
                return null;
            }
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(String.format("Cannot convert '%s' to an integer",
                    value));
        }

        try {
            viewInteraction.perform(typeText(value));
        } catch (PerformException e) {
            throw new InvalidElementStateException("sendKeys", params.getElementId(), e);
        }

        return null;
    }
}
