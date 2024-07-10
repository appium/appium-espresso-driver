package io.appium.espressoserver.lib.helpers.w3c.dispatcher

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import java.util.concurrent.Callable

abstract class BaseDispatchResult {
    var next: Callable<BaseDispatchResult>? = null
    operator fun hasNext(): Boolean {
        return next != null
    }

    @Throws(AppiumException::class)
    abstract fun perform()
}