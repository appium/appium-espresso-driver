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

package io.appium.espressoserver.lib.helpers;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException;
import io.appium.espressoserver.lib.viewaction.ViewGetter;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

public class IMEHelpers {
    private static final Map<String, Integer> ACTION_CODES_MAP = new HashMap<>();

    static {
        ACTION_CODES_MAP.put("normal", 0);
        ACTION_CODES_MAP.put("unspecified", 0);
        ACTION_CODES_MAP.put("none", 1);
        ACTION_CODES_MAP.put("go", 2);
        ACTION_CODES_MAP.put("search", 3);
        ACTION_CODES_MAP.put("send", 4);
        ACTION_CODES_MAP.put("next", 5);
        ACTION_CODES_MAP.put("done", 6);
        ACTION_CODES_MAP.put("previous", 7);
    }

    private static int toActionCode(Object action) throws AppiumException {
        if (action instanceof Long) {
            return ((Long) action).intValue();
        }
        if (action instanceof String) {
            Integer result = ACTION_CODES_MAP.get(((String) action).toLowerCase());
            if (result == null) {
                throw new InvalidArgumentException(String.format("The action value can be one " +
                        "of %s. '%s' is given instead", ACTION_CODES_MAP.keySet(), action));
            }
            return result;
        }
        throw new InvalidArgumentException(String.format("The action value can be either an integer " +
                "action code or one of %s. '%s' is given instead", ACTION_CODES_MAP.keySet(), action));
    }

    public static void performEditorAction(@Nullable Object action) throws AppiumException {
        ViewInteraction viewInteraction;
        try {
            viewInteraction = onView(hasFocus());
        } catch (NoMatchingViewException e) {
            throw new InvalidElementStateException(String.format("Currently there is no focused " +
                    "element to perform %s editor action on", action == null ? "the default" : action), e);
        }

        if (action == null) {
            logger.debug("Performing the default editor action on the focused element");
            try {
                viewInteraction.perform(pressImeActionButton());
                return;
            } catch (PerformException e) {
                throw new InvalidElementStateException("Cannot perform the default action " +
                        "on the focused element");
            }
        }

        int actionCode = toActionCode(action);
        logger.debug(String.format("Performing editor action %s on the focused element", actionCode));
        View view = new ViewGetter().getView(viewInteraction);
        InputConnection ic = view.onCreateInputConnection(new EditorInfo());
        if (!ic.performEditorAction(actionCode)) {
            throw new InvalidElementStateException(String.format("Cannot perform editor action %s " +
                    "on the focused element", action));
        }
    }
}
