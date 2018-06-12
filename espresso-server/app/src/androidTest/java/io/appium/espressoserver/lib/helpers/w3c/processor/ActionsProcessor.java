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

import java.util.ArrayList;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;

import static io.appium.espressoserver.lib.helpers.w3c.processor.KeyProcessor.processKeyAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processNullAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PointerProcessor.processPointerAction;

@SuppressWarnings("unused")
public class ActionsProcessor {

    /**
     * Follows algorithm "for process an input source action sequence" in section 17.3
     */
    public static List<ActionObject> processSourceActionSequence(InputSource inputSource, ActiveInputSources activeInputSources, InputStateTable inputStateTable)
            throws InvalidArgumentException, NotYetImplementedException {
        // 1: Get the type
        InputSourceType type = inputSource.getType();

        // 2: If type is not "key", "pointer", or "none", return an error
        if (type == null) {
            throw new InvalidArgumentException("'type' is required in input source and must be one of: pointer, key, none");
        }

        // 3: Get the ID from the input source
        String id = inputSource.getId();

        // 4: If id is undefined or is not a String, return error
        if (id == null) {
            throw new InvalidArgumentException("'id' in action cannot be null");
        }

        // 6: Let source be the input source in the list of active input sources where that input source’s input id matches id,
        InputSource activeSource = activeInputSources.getInputSource(inputSource);

        // 7:  source is undefined:
        if (activeSource == null) {
            activeInputSources.addInputSource(inputSource);
            inputStateTable.addInputState(inputSource.getId(), inputSource.getDefaultState());
        } else {

            // 8: If source's type does not match type return an error
            if (activeSource.getType() != inputSource.getType()) {
                throw new InvalidArgumentException(String.format("Input type %s does not match pre-existing input type '%s' in actions input source with id '%s'",
                        inputSource.getType(), activeSource.getType(),  id));
            }

            // 9: If it's a pointer type, check that they match parameter types
            if (activeSource.getType() == InputSourceType.POINTER && activeSource.getPointerType() != inputSource.getPointerType()) {
                throw new InvalidArgumentException(String.format("Pointer type %s does not match pre-existing pointer type '%s' in actions input source with id '%s'",
                        inputSource.getPointerType(), activeSource.getPointerType(),id));
            }
        }

        // 10: Let action items be the result of getting a property named actions from action sequence
        List<Action> actionItems = inputSource.getActions();

        // 11: If action items is not an Array, return error
        if (actionItems == null) {
            throw new InvalidArgumentException(String.format("'actions' array not provided in actions input source with id '%s'", id));
        }

        // 12: Let actions be a new list
        List<ActionObject> actionObjects = new ArrayList<>();

        // 13: For each item in action items
        int index = 0;
        for (Action action:actionItems) {
            if (action == null) {
                throw new InvalidArgumentException(String.format("'actions[%s]' did not provide a valid JSON object for actions input source with id '%s'", index, id));
            }
            index++;
            switch (inputSource.getType()) {
                case NONE:
                    actionObjects.add(processNullAction(action, inputSource.getType(), id, index));
                    break;
                case POINTER:
                    actionObjects.add(processPointerAction(action, inputSource, id, index));
                    break;
                case KEY:
                    actionObjects.add(processKeyAction(action, inputSource.getType(), id, index));
                    break;
                default:
                    throw new InvalidArgumentException(String.format("'actions[%s]' of input source with id '%s' did not provide a valid type", index, id));
            }
        }

        return actionObjects;
    }
}
