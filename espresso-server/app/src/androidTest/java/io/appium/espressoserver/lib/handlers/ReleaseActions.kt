package io.appium.espressoserver.lib.handlers

import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.models.Actions
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable


class ReleaseActions : RequestHandler<AppiumParams, Void?> {

    @Throws(AppiumException::class)
    override fun handle(params: AppiumParams): Void? {

        val runnable = UiControllerRunnable<Void> {
            Actions().release(params.sessionId)
            null
        }

        UiControllerPerformer(runnable).run()
        return null
    }
}
