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

package io.appium.espressoserver.lib.compose

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import io.appium.espressoserver.lib.handlers.exceptions.ScreenCaptureException
import java.io.ByteArrayOutputStream

internal object ComposeScreenshot {
    fun takeComposeNodeScreenshot(nodeInteraction: SemanticsNodeInteraction): String =
        try {
            encodeBitmap(
                nodeInteraction.captureToImage().asAndroidBitmap(),
            ) { nodeInteraction.fetchSemanticsNode().toString() }
        } catch (e: RuntimeException) {
            throw ScreenCaptureException("Cannot take a screenshot of a node", e)
        }

    private fun encodeBitmap(bitmap: Bitmap?, targetNameSupplier: () -> String): String {
        if (bitmap == null || bitmap.height == 0 || bitmap.width == 0) {
            throw ScreenCaptureException(
                "Cannot capture a shot of the ${targetNameSupplier()}. " +
                    "Make sure none of the currently visible " +
                    "views have FLAG_SECURE set and that it is possible to take a screenshot manually",
            )
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
