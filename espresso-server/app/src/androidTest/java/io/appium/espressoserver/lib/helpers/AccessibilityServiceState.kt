package io.appium.espressoserver.lib.helpers

import android.app.UiAutomation
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator

fun setAccessibilityServiceState() {
    InstrumentationRegistry.getArguments().getString("DISABLE_SUPPRESS_ACCESSIBILITY_SERVICES")?.let { flag ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (flag.toBoolean()) {
                true ->
                    Configurator.getInstance().uiAutomationFlags = UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
                false ->
                    Configurator.getInstance().uiAutomationFlags = 0
            }
        }
    }
}
