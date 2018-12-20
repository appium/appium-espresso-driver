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

package io.appium.espressoserver.lib.helpers;


import android.graphics.Bitmap;
import android.util.Base64;
import android.view.View;

import java.io.ByteArrayOutputStream;

import androidx.test.runner.screenshot.ScreenCapture;
import androidx.test.runner.screenshot.Screenshot;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.ElementNotVisibleException;
import io.appium.espressoserver.lib.handlers.exceptions.ScreenCaptureException;
import io.appium.espressoserver.lib.model.ViewElement;

public class ScreenshotsHelper {
    private final View view;

    public ScreenshotsHelper() {
        this.view = null;
    }

    public ScreenshotsHelper(View view) {
        this.view = view;
    }

    /**
     * Makes a screenshot of the particular view.
     *
     * @return the screenshot of the view as base-64 encoded string.
     * @throws ElementNotVisibleException if the view has no visible area.
     * @throws ScreenCaptureException if it is impossible to take a screenshot.
     */
    public String getScreenshot() throws AppiumException {
        if (view != null && new ViewElement(view).getBounds().isEmpty()) {
            throw new ElementNotVisibleException(
                    String.format("Cannot get a screenshot of the invisible %s", view.getClass().getName()));
        }

        final ScreenCapture screenCap = view == null ? Screenshot.capture() : Screenshot.capture(view);
        final Bitmap bitmapScreenCap = screenCap.getBitmap();
        if (bitmapScreenCap == null || bitmapScreenCap.getHeight() == 0 || bitmapScreenCap.getWidth() == 0) {
            throw new ScreenCaptureException(String.format("Cannot capture a shot of the %s. " +
                            "Make sure none of the currently visible " +
                            "views have FLAG_SECURE set and that it is possible to take a screenshot manually",
                    view == null ? "current screen" : view.getClass().getName()));
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapScreenCap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }
}
