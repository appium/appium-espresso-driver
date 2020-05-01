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

package io.appium.espressoserver.lib.handlers

import android.os.SystemClock
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration

import androidx.test.espresso.InjectEventSecurityException
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.model.KeyEventParams
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable

class PressKeyCode(private val isLongPress: Boolean) : RequestHandler<KeyEventParams, Void?> {

    private val LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout()

    @Throws(AppiumException::class)
    override fun handleInternal(params: KeyEventParams): Void? {
        val runnable = object : UiControllerRunnable<Void?> {
            override fun run(uiController: UiController): Void? {
                val keyCode = params.keycode
                val metaState = params.metastate
                val flags = params.flags
                val downTime = SystemClock.uptimeMillis()

                try {
                    var isSuccessful = uiController.injectKeyEvent(
                            KeyEvent(
                                    downTime,
                                    downTime,
                                    KeyEvent.ACTION_DOWN,
                                    keyCode,
                                    0,
                                    metaState,
                                    KeyCharacterMap.VIRTUAL_KEYBOARD,
                                    0,
                                    flags
                            )
                    )

                    if (isLongPress) {
                        // https://developer.android.com/reference/android/view/KeyEvent#FLAG_LONG_PRESS
                        // The FLAG_LONG_PRESS flag is set after the first key repeat that occurs after the long press timeout
                        isSuccessful = isSuccessful and uiController.injectKeyEvent(
                                KeyEvent(downTime, SystemClock.uptimeMillis() + LONG_PRESS_TIMEOUT,
                                        KeyEvent.ACTION_DOWN, keyCode, 1, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                                        0, flags or KeyEvent.FLAG_LONG_PRESS))
                    }


                    isSuccessful = isSuccessful and uiController.injectKeyEvent(KeyEvent(downTime,
                            SystemClock.uptimeMillis() + if (isLongPress) LONG_PRESS_TIMEOUT else 0,
                            KeyEvent.ACTION_UP, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD,
                            0, flags))

                    if (!isSuccessful) {
                        throw InvalidArgumentException(String.format(
                                "Cannot generate long key press event for key code %s", keyCode))
                    }
                } catch (ie: InjectEventSecurityException) {
                    throw AppiumException(String.format("Could not inject key code %s. Reason: %s", keyCode, ie.cause))
                }

                return null
            }
        }

        UiControllerPerformer(runnable).run()
        return null
    }
}
