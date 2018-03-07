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

import android.app.Dialog;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NoAlertOpenException;
import io.appium.espressoserver.lib.model.AppiumParams;

import static android.text.TextUtils.join;

public class GetAlertText implements RequestHandler<AppiumParams, String> {

    @Override
    public String handle(AppiumParams params) throws AppiumException {
        // We use UIA2 here, since Espresso is limited to application sandbox
        // and cannot handle security alerts
        final UiDevice mDevice = UiDevice
                .getInstance(InstrumentationRegistry.getInstrumentation());
        final List<UiObject2> dialogs = mDevice.findObjects(By.clazz(Dialog.class));
        if (dialogs.isEmpty()) {
            throw new NoAlertOpenException();
        }
        final List<UiObject2> elementsWithText = dialogs.get(0)
                .findObjects(By.text(Pattern.compile("\\S+")));
        if (elementsWithText.isEmpty()) {
            return "";
        }
        final List<String> result = new ArrayList<>();
        for (final UiObject2 element : elementsWithText) {
            if (!element.getClassName().contains("Button")) {
                result.add(element.getText());
            }
        }
        return join("\n", result);
    }
}
