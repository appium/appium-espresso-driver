package io.appium.espressoserver.lib.helpers

import android.app.UiAutomation
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator

fun setAccessibilityServiceState() {
    InstrumentationRegistry.getArguments().getString("DISABLE_SUPPRESS_ACCESSIBILITY_SERVICES")?.let { flag ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Configurator.getInstance().uiAutomationFlags =
                    if (flag.toBoolean()) UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES else 0
        }
    }
}
