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

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider

fun getCurrentWindowRect(): Rect {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val windowManager =
        (context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
            ?: throw IllegalStateException("Could not retrieve Window Manager Service instance")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.currentWindowMetrics.bounds
        return Rect(0, 0, bounds.width(), bounds.height())
    }
    val displayMetrics = DisplayMetrics()
    @Suppress("DEPRECATION")
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
}
