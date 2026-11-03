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

package io.appium.espressoserver.test.helpers

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import io.appium.espressoserver.lib.helpers.windowOriginOnScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(RobolectricTestRunner::class)
class `Window Origin Tests` {

    @Test
    fun `window origin should not depend on the view offset or translation`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val view = View(activity)
        activity.setContentView(
            FrameLayout(activity).apply { addView(view, FrameLayout.LayoutParams(100, 100)) }
        )
        val originBefore = view.windowOriginOnScreen()
        val locationBefore = IntArray(2).also { view.getLocationOnScreen(it) }

        view.translationY = 80f
        view.translationX = 30f

        val locationAfter = IntArray(2).also { view.getLocationOnScreen(it) }
        assertEquals(locationBefore[1] + 80, locationAfter[1])
        assertNotEquals(locationBefore[0], locationAfter[0])
        assertEquals(originBefore, view.windowOriginOnScreen())
    }
}
