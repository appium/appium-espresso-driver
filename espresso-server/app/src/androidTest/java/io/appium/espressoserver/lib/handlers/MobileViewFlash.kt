package io.appium.espressoserver.lib.handlers

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.test.platform.app.InstrumentationRegistry
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.ViewFlashParams
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MobileViewFlash : RequestHandler<ViewFlashParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: ViewFlashParams): Void? {

        val duration = params.durationMillis
        val repeatCount = params.repeatCount

        val view = Element.getViewById(params.elementId)
        val latch = CountDownLatch(1)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val animation = AlphaAnimation(1f, 0f)
            animation.repeatMode = Animation.REVERSE
            animation.duration = duration.toLong()
            animation.repeatCount = repeatCount
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    // Unused
                }

                override fun onAnimationEnd(animation: Animation) {
                    latch.countDown()
                }

                override fun onAnimationRepeat(animation: Animation) {
                    // Unused
                }
            })
            view.startAnimation(animation)
        }
        try {
            latch.await(1, TimeUnit.MINUTES)
        } catch (e: InterruptedException) {
            throw AppiumException(e)
        }

        return null
    }
}
