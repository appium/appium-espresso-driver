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
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import java.lang.IllegalStateException

fun getCurrentWindowRect(): Rect {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val winManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        ?: throw IllegalStateException("Couldn't retrieve Window Manager Service instance: " +
                "context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE) is null")
    return winManager.currentWindowMetrics.bounds
}
