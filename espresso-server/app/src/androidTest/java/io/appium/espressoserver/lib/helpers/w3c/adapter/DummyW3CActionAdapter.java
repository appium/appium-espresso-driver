package io.appium.espressoserver.lib.helpers.w3c.adapter;

import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch;

public class DummyW3CActionAdapter extends BaseW3CActionAdapter {

    public boolean keyDown(KeyDispatch.KeyEvent keyEvent) {
        return true;
    }

    public boolean keyUp(KeyDispatch.KeyEvent keyEvent) {
        return true;
    }

}
