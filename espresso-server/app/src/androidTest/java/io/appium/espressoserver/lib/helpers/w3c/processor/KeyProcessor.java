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

package io.appium.espressoserver.lib.helpers.w3c.processor;

import java.util.Arrays;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processPauseAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.ProcessorHelpers.throwArgException;

@SuppressWarnings("unused")
public class KeyProcessor {

    /**
     * Follows the 'process a key action' algorithm in 17.2
     * @param action Action being processed
     * @param inputSourceType Source type
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    public static ActionObject processKeyAction(Action action, InputSourceType inputSourceType, String id, int index)
            throws InvalidArgumentException {

        // 1-3 get and validate the action type
        ActionType subType = action.getType();
        ActionType[] validKeyTypes = new ActionType[]{ KEY_UP, KEY_DOWN, PAUSE };
        if (!Arrays.asList(validKeyTypes).contains(subType)) {
            throwArgException(index, id, "has an invalid type. 'type' for 'key' actions must be one of: keyUp, keyDown or pause");
        }

        // 4 if pause return PAUSE action
        if (subType.equals(PAUSE)) {
            return processPauseAction(action, inputSourceType, id, index);
        }

        // 5-7 get the Unicode value of the keystroke (verify that it's a single character)
        String key = action.getValue();

        if (key.length() != 1) {
            throwArgException(index, id, String.format("has invalid 'value' %s. Must be a unicode point", key));
        }


        ActionObject actionObject = new ActionObject(id, inputSourceType, subType, index);
        actionObject.setValue(key);
        return actionObject;
    }
}
