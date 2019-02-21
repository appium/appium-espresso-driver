package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.Actions
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable


class PerformAction : RequestHandler<Actions, Void?> {

    @Throws(AppiumException::class)
    override fun handle(actions: Actions): Void? {

        val runnable = UiControllerRunnable<Void> { uiController ->
            actions.adapter = EspressoW3CActionAdapter(uiController)
            actions.perform(actions.sessionId)
            null
        }

        UiControllerPerformer(runnable).run()
        return null
    }
}
