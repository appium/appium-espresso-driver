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

package io.appium.espressoserver.lib.helpers

import android.graphics.Bitmap
import android.util.Base64
import android.view.View

import java.io.ByteArrayOutputStream
import androidx.test.runner.screenshot.Screenshot
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.ElementNotVisibleException
import io.appium.espressoserver.lib.handlers.exceptions.ScreenCaptureException
import io.appium.espressoserver.lib.model.ViewElement

class ScreenshotsHelper @JvmOverloads constructor(private val view: View? = null) {

    /**
     * Makes a screenshot of the particular view.
     *
     * @return the screenshot of the view as base-64 encoded string.
     * @throws ElementNotVisibleException if the view has no visible area.
     * @throws ScreenCaptureException if it is impossible to take a screenshot.
     */
    val screenshot: String
        @Throws(AppiumException::class)
        get() {
            if (view != null && ViewElement(view).bounds.isEmpty) {
                throw ElementNotVisibleException(
                        String.format("Cannot get a screenshot of the invisible %s", view.javaClass.name))
            }

            val screenCap = if (view == null) Screenshot.capture() else Screenshot.capture(view)
            val bitmapScreenCap = screenCap.bitmap
            if (bitmapScreenCap == null || bitmapScreenCap.height == 0 || bitmapScreenCap.width == 0) {
                throw ScreenCaptureException("Cannot capture a shot of the " +
                        "${if (view == null) "current screen" else view.javaClass.name}. " +
                        "Make sure none of the currently visible " +
                        "views have FLAG_SECURE set and that it is possible to take a screenshot manually")
            }
            val outputStream = ByteArrayOutputStream()
            bitmapScreenCap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }
}
