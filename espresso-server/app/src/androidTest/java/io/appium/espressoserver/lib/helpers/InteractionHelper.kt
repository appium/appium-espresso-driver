package io.appium.espressoserver.lib.helpers

import android.app.UiAutomation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import io.appium.espressoserver.lib.helpers.KReflectionUtils.extractMethod
import io.appium.espressoserver.lib.helpers.KReflectionUtils.invokeMethod

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
