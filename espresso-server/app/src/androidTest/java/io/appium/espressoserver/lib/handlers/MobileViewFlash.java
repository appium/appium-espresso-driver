package io.appium.espressoserver.lib.handlers;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

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
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Animation animation = new AlphaAnimation(1, 0);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setDuration(duration);
                animation.setRepeatCount(repeatCount);
                view.startAnimation(animation);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        return null;
    }
}
