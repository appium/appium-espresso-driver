package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.ActivityHelpers
import io.appium.espressoserver.lib.helpers.InvocationOperation
import io.appium.espressoserver.lib.helpers.reflection.ClassUtils
import io.appium.espressoserver.lib.model.EspressoElement
import io.appium.espressoserver.lib.model.InvocationTarget
import io.appium.espressoserver.lib.model.MobileBackdoorParams

class MobileBackdoor : RequestHandler<MobileBackdoorParams, Any?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: MobileBackdoorParams): Any? {
        params.target?.let {target ->
            val activity = ActivityHelpers.currentActivity
            val ops = getBackdoorOperations(params)

            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            val result = when (target) {
                InvocationTarget.ACTIVITY -> invokeBackdoorMethods(activity, ops)
                InvocationTarget.APPLICATION -> invokeBackdoorMethods(activity.application, ops)
                InvocationTarget.ELEMENT -> invokeBackdoorMethods(EspressoElement.getViewById(params.targetElement), ops)
                else -> throw InvalidArgumentException("target cannot be '$target'")
            } ?: return null

            if (result is Array<*> && result.filterNotNull()
                    .all { it is String || (ClassUtils.wrapperToPrimitive(it.javaClass) != null) }
            ) {
                return result
            }

            ClassUtils.wrapperToPrimitive(result.javaClass)?.let { return result }
                ?: return result.toString()
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
