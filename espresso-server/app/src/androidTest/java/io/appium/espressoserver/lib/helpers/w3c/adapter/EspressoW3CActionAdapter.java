package io.appium.espressoserver.lib.helpers.w3c.adapter;

import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.KeyEvent;

import static android.view.KeyEvent.ACTION_DOWN;

public class EspressoW3CActionAdapter extends BaseW3CActionAdapter {

    private final UiController uiController;

    public EspressoW3CActionAdapter(UiController uiController) {
        this.uiController = uiController;
    }

    public boolean keyDown(KeyEvent keyEvent) throws AppiumException {
        android.view.KeyEvent androidKeyEvent = new android.view.KeyEvent(
                System.currentTimeMillis(), System.currentTimeMillis(),
                ACTION_DOWN, Character.getNumericValue(keyEvent.getKey().charAt(0)),
                0
        );

        try {
            uiController.injectKeyEvent(androidKeyEvent);
        } catch (InjectEventSecurityException e) {
            throw new AppiumException(e.getCause().toString());
        }
        return false;
    }

    public boolean keyUp(KeyEvent keyEvent) {
        return false;
    }
}
