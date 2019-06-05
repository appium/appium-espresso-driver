package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.Actions
import io.appium.espressoserver.lib.model.MultiTouchActionsParams
import io.appium.espressoserver.lib.model.TouchAction.Companion.toW3CInputSources
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable

class MultiTouchAction : RequestHandler<MultiTouchActionsParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: MultiTouchActionsParams): Void? {
        val runnable = UiControllerRunnable<Void> { uiController ->
            val inputSources = toW3CInputSources(params.actions)
            val actions = Actions.ActionsBuilder()
                    .withAdapter(EspressoW3CActionAdapter(uiController))
                    .withActions(inputSources)
                    .build()

            actions.perform(params.sessionId)
            actions.release(params.sessionId)
            null
        }

        UiControllerPerformer(runnable).run()
        return null
    }
}
