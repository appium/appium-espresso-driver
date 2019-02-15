package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.TouchAction

data class MultiTouchActionsParams(var actions: List<List<TouchAction>>? = null) : AppiumParams()