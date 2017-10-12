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

import android.graphics.Bitmap;
import android.util.Base64;
import android.view.View;

import java.io.ByteArrayOutputStream;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class Screenshot implements RequestHandler<AppiumParams, String> {

    @Override
    public String handle(AppiumParams params) throws AppiumException {
        return takeScreenshot();
    }


    private String takeScreenshot() throws AppiumException {
        try {
            // Create bitmap screen capture
            View rootView = (new ViewFinder()).getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmapScreenCap = Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.setDrawingCacheEnabled(false);

            // Stream the bitmap to byte array stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int quality = 100;
            bitmapScreenCap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
            byte[] bytes = outputStream.toByteArray();

            // Encode the byte array stream to base 64
            return Base64.encodeToString(bytes, Base64.DEFAULT);

        } catch (Exception e) {
            throw new AppiumException(String.format("Could not get screenshot %s", e.getCause()));
        }
    }

}
