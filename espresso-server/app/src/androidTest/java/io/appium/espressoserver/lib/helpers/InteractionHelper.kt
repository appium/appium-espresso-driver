package io.appium.espressoserver.lib.helpers

import android.app.UiAutomation
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice

object InteractionHelper {
    private var uiDevice: UiDevice? = null

    @Synchronized
    fun getUiDevice(): UiDevice {
        if (uiDevice == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // The flag is necessary not to stop running accessibility service
                // https://github.com/appium/appium/issues/4910
                // https://developer.android.com/reference/android/app/UiAutomation.html#FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
                Configurator.getInstance().uiAutomationFlags = UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
            }
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        }
        return uiDevice!!
    }
}
