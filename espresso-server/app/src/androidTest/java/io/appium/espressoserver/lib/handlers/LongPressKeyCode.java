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
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.model.KeyEventParams;

import static io.appium.espressoserver.lib.helpers.InteractionHelper.injectEventSync;

public class LongPressKeyCode implements RequestHandler<KeyEventParams, Void> {

    @Override
    public Void handle(KeyEventParams params) throws AppiumException {
        final int keyCode = params.getKeycode();
        Integer metaState = params.getMetastate();
        metaState = metaState == null ? 0 : metaState;
        Integer flags = params.getFlags();
        flags = flags == null ? 0 : flags;

        final long downTime = SystemClock.uptimeMillis();
        boolean isSuccessful = injectEventSync(new KeyEvent(downTime, downTime,
                KeyEvent.ACTION_DOWN, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                0, flags));
        // https://android.googlesource.com/platform/frameworks/base.git/+/9d83b4783c33f1fafc43f367503e129e5a5047fa%5E%21/#F0
        isSuccessful &= injectEventSync(new KeyEvent(downTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN, keyCode, 1, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                0, flags | KeyEvent.FLAG_LONG_PRESS));
        isSuccessful &= injectEventSync(new KeyEvent(downTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                0, flags));
        if (!isSuccessful) {
            throw new InvalidArgumentException(String.format(
                    "Cannot generate long key press event for key code %s", keyCode));
        }

        return null;
    }
}
