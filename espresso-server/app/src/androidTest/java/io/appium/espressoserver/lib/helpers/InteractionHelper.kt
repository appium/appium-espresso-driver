package io.appium.espressoserver.lib.helpers

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

object InteractionHelper {
    private var uiDevice: UiDevice? = null

    @Synchronized
    fun getUiDevice(): UiDevice {
        if (uiDevice == null) {
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        }
        return uiDevice!!
    }
}
