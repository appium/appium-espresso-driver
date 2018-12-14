package io.appium.espressoserver.lib.handlers;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.test.platform.app.InstrumentationRegistry;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.ViewFlashParams;

public class MobileViewFlash implements RequestHandler<ViewFlashParams, Void> {

    private static final int DURATION_MILLIS = 30;
    private static final int REPEAT_COUNT = 15;

    @Override
    public Void handle(final ViewFlashParams params) throws AppiumException {

        final int duration = params.getDurationMillis() == null ? DURATION_MILLIS : params.getDurationMillis();
        final int repeatCount = params.getRepeatCount() == null ? REPEAT_COUNT : params.getRepeatCount();

        final View view = Element.getViewById(params.getElementId());
        final CountDownLatch latch = new CountDownLatch(1);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Animation animation = new AlphaAnimation(1, 0);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setDuration(duration);
                animation.setRepeatCount(repeatCount);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // Unused
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        latch.countDown();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // Unused
                    }
                });
                view.startAnimation(animation);
            }
        });
        try {
            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new AppiumException(e);
        }
        return null;
    }
}
