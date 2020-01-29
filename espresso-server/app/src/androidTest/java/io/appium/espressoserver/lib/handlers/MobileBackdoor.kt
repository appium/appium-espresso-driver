package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.ActivityHelpers
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.InvocationOperation
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.MobileBackdoorParams
import io.appium.espressoserver.lib.model.MobileBackdoorParams.Companion.InvocationTarget.*

class MobileBackdoor : RequestHandler<MobileBackdoorParams, Any?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: MobileBackdoorParams): Any? {
        AndroidLogger.logger.info("Invoking Backdoor")
        params.target?.let {target ->
            val activity = ActivityHelpers.currentActivity
            val ops = getBackdoorOperations(params)

            when (target) {
                ACTIVITY -> return invokeBackdoorMethods(activity, ops)
                APPLICATION -> return invokeBackdoorMethods(activity.application, ops)
                ELEMENT -> return invokeBackdoorMethods(Element.getViewById(params.targetElement), ops)
                else -> throw InvalidArgumentException("target cannot be '$target'")
            }
        }

        throw InvalidArgumentException("Target must not be empty and must be of type: 'activity', 'application'")
    }

    @Throws(AppiumException::class)
    private fun invokeBackdoorMethods(invokeOn: Any, ops: List<InvocationOperation>): Any? {
        return ops.fold(invokeOn) { invocationTarget, operation -> operation.apply(invocationTarget) }
    }

    @Throws(InvalidArgumentException::class)
    private fun getBackdoorOperations(params: MobileBackdoorParams): List<InvocationOperation> {
        return params.methods.map {method ->
            InvocationOperation(method.name, method.arguments, method.argumentTypes)
        }
    }
}
