package io.appium.espressoserver.lib.model

import java.util.Arrays


enum class OrientationType {
    LANDSCAPE,
    PORTRAIT;

    companion object {
        fun supportedOrientationTypes(): String {
            return Arrays.toString(values())
        }
    }
}
