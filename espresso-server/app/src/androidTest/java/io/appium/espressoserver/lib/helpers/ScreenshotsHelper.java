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

import io.appium.espressoserver.lib.model.ViewElement;

public class ScreenshotsHelper {
    private final View view;

    public ScreenshotsHelper(View view) {
        this.view = view;
    }

    /**
     * Makes a screenshot of the particular view.
     *
     * @param quality defines the quality of the output image. Should be in range 1..100, where
     *                100 is the best quality.
     * @return the screenshot of the view as base-64 encoded string.
     * @throws IllegalStateException if the view has no visible area.
     */
    public String getScreenshot(int quality) {
        if (new ViewElement(view).getBounds().isEmpty()) {
            throw new IllegalStateException("Cannot get a screenshot of the invisible view");
        }

        view.setDrawingCacheEnabled(true);
        final Bitmap bitmapScreenCap;
        try {
            bitmapScreenCap = Bitmap.createBitmap(view.getDrawingCache());
        } finally {
            view.setDrawingCacheEnabled(false);
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapScreenCap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public String getScreenshot() {
        return getScreenshot(100);
    }
}
