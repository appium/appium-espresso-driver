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
import android.widget.Button;

import java.util.List;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException;
import io.appium.espressoserver.lib.handlers.exceptions.NoAlertOpenException;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.model.AppiumParams;

public class AcceptAlert implements RequestHandler<AppiumParams, Void> {

    @Override
    @Nullable
    public Void handle(AppiumParams params) throws AppiumException {
        // We use UIA2 here, since Espresso is limited to application sandbox
        // and cannot handle security alerts
        final UiDevice mDevice = UiDevice
                .getInstance(InstrumentationRegistry.getInstrumentation());
        final List<UiObject2> dialogs = mDevice.findObjects(By.clazz(Dialog.class));
        if (dialogs.isEmpty()) {
            throw new NoAlertOpenException();
        }
        // TODO: Is the first button always the one we need to click in order to accept the alert?
        final List<UiObject2> buttons = dialogs.get(0)
                .findObjects(By.clazz(Button.class));
        if (buttons.isEmpty()) {
            throw new InvalidElementStateException("No buttons can be detected on the alert");
        }
        Logger.info(String.format("Clicking dialog button '%s' in order to accept it",
                buttons.get(0).getText()));
        buttons.get(0).click();
        return null;
    }
}
