package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.ActivityHelper
import io.appium.espressoserver.lib.helpers.InvocationOperation
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.MobileBackdoorParams

import io.appium.espressoserver.lib.helpers.AndroidLogger.logger

class MobileBackdoor : RequestHandler<MobileBackdoorParams, Any?> {

    @Throws(AppiumException::class)
    override fun handle(params: MobileBackdoorParams): Any? {
        logger.info("Invoking Backdoor")
        if (params.target == null) {
            throw InvalidArgumentException("Target must not be empty and must be of type: 'activity', 'application'")
        }

        val activity = ActivityHelper.getCurrentActivity()
        val ops = getBackdoorOperations(params)

        when (params.target) {
            MobileBackdoorParams.InvocationTarget.ACTIVITY -> return invokeBackdoorMethods(activity, ops)
            MobileBackdoorParams.InvocationTarget.APPLICATION -> return invokeBackdoorMethods(activity.application, ops)
            MobileBackdoorParams.InvocationTarget.ELEMENT -> return invokeBackdoorMethods(Element.getViewById(params.elementId), ops)
            else -> throw InvalidArgumentException("target cannot be ${params.target}")
        }

    }

    @Throws(AppiumException::class)
    private fun invokeBackdoorMethods(invokeOn: Any, ops: List<InvocationOperation>): Any? {
        var invocationResult: Any? = null
        var invocationTarget: Any? = invokeOn
        ops.forEach {
            invocationResult = it.apply(invocationTarget)
            invocationTarget = invocationResult
        }

        return invocationResult

    }

    @Throws(InvalidArgumentException::class)
    private fun getBackdoorOperations(params: MobileBackdoorParams): List<InvocationOperation> {
        return params.methods.map {
            val methodName = it.name ?: throw InvalidArgumentException("'name' is a required parameter for backdoor method to be invoked.")
            InvocationOperation(methodName, it.arguments, it.argumentTypes)
        }
    }

}
