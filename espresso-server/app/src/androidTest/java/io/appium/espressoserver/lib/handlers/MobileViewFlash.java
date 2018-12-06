package io.appium.espressoserver.lib.handlers;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.test.platform.app.InstrumentationRegistry;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;

public class MobileViewFlash implements RequestHandler<AppiumParams,Void> {
    @Override
    public Void handle(AppiumParams params) throws AppiumException {
        final View view = Element.getViewById(params.getElementId());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Animation animation = new AlphaAnimation(1, 0);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setDuration(200);
                animation.setRepeatCount(5);
                view.startAnimation(animation);
            }
        });
        return null;
    }
}
