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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.model.KeyEventParams;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;

import static io.appium.espressoserver.lib.helpers.InteractionHelper.injectEventSync;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY;

public class PressKeyCode implements RequestHandler<KeyEventParams, Void> {

    private final boolean isLongPress;

    // Make LONG_PRESS the long press timeout plus an extra 100ms so that it's above the threshold
    private final int LONG_PRESS_DURATION = ViewConfiguration.getLongPressTimeout() + 100;

    // Make SHORT_PRESS an arbitrarily low number (10 ms)
    private final int SHORT_PRESS_DURATION = 10;

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

                AndroidLogger.logger.info("meta state with shift on");

                try {
                    boolean isSuccessful = uiController.injectKeyEvent(new KeyEvent(downTime, downTime,
                            KeyEvent.ACTION_DOWN, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                            0, flags));


                    isSuccessful &= uiController.injectKeyEvent(new KeyEvent(downTime,
                            downTime + (isLongPress ? LONG_PRESS_DURATION : SHORT_PRESS_DURATION),
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
