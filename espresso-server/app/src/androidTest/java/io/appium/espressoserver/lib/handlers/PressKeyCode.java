/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.handlers;

import android.os.SystemClock;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.model.KeyEventParams;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;

public class PressKeyCode implements RequestHandler<KeyEventParams, Void> {

    private final boolean isLongPress;

    private final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();

    public PressKeyCode(boolean isLongPress) {
        this.isLongPress = isLongPress;
    }

    @Override
    public Void handle(final KeyEventParams params) throws AppiumException {
        UiControllerRunnable<Void> runnable = new UiControllerRunnable<Void>() {
            @Override
            public Void run(UiController uiController) throws AppiumException {
                final int keyCode = params.getKeycode();
                Integer metaState = params.getMetastate();
                metaState = metaState == null ? 0 : metaState;
                Integer flags = params.getFlags();
                flags = flags == null ? 0 : flags;
                final long downTime = SystemClock.uptimeMillis();

                try {
                    boolean isSuccessful = uiController.injectKeyEvent(new KeyEvent(downTime, downTime,
                            KeyEvent.ACTION_DOWN, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                            0, flags));

                    if (isLongPress) {
                        // https://developer.android.com/reference/android/view/KeyEvent#FLAG_LONG_PRESS
                        // The FLAG_LONG_PRESS flag is set after the first key repeat that occurs after the long press timeout
                        isSuccessful &= uiController.injectKeyEvent(new KeyEvent(downTime, SystemClock.uptimeMillis() + LONG_PRESS_TIMEOUT,
                                KeyEvent.ACTION_DOWN, keyCode, 1, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                                0, flags | KeyEvent.FLAG_LONG_PRESS));
                    }


                    isSuccessful &= uiController.injectKeyEvent(new KeyEvent(downTime,
                            SystemClock.uptimeMillis() + (isLongPress ? LONG_PRESS_TIMEOUT : 0),
                            KeyEvent.ACTION_UP, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                            0, flags));

                    if (!isSuccessful) {
                        throw new InvalidArgumentException(String.format(
                                "Cannot generate long key press event for key code %s", keyCode));
                    }
                } catch (InjectEventSecurityException ie) {
                    throw new AppiumException(String.format("Could not inject key code %s. Reason: %s", keyCode, ie.getCause()));
                }

                return null;
            }
        };

        new UiControllerPerformer<>(runnable).run();
        return null;
    }
}
