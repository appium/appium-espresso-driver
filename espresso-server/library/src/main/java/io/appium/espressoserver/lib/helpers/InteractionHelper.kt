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

import android.app.UiAutomation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import io.appium.espressoserver.lib.helpers.ReflectionUtils.extractMethod
import io.appium.espressoserver.lib.helpers.ReflectionUtils.invokeMethod

object InteractionHelper {
    private var uiDevice: UiDevice? = null

    @Synchronized
    fun getUiDevice(): UiDevice {
        if (uiDevice == null) {
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        }
        return uiDevice!!
    }

    fun getUiAutomation(): UiAutomation {
        val getUiAutomation = extractMethod(UiDevice::class.java, "getUiAutomation")
        return invokeMethod(getUiDevice(), getUiAutomation) as UiAutomation
    }
}
