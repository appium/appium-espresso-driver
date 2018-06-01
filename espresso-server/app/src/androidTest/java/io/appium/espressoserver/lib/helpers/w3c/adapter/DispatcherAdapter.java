package io.appium.espressoserver.lib.helpers.w3c.adapter;

import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDown.KeyDownEvent;

public interface DispatcherAdapter {

    public boolean keyDown(KeyDownEvent keyDownEvent);
}
