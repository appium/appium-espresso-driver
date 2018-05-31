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

package io.appium.espressoserver.lib.helpers.w3c.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.*;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.*;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.VIEWPORT;

@SuppressWarnings("unused")
public class W3CActions {
    private List<InputSource> actions = null;

    @Nullable
    public List<InputSource> getActions() {
        return actions;
    }

    public void setActions(List<InputSource> actions) {
        this.actions = actions;
    }

    /**
     * Follows algorithm "for process an input source action sequence" in section 17.3
     */
    public void processSourceActionSequence(InputSource inputSource, ActiveInputSources activeInputSources, InputStateTable inputStateTable) throws InvalidArgumentException {
        // 1: Get the type
        InputSourceType type = inputSource.getType();

        // 2: If type is not "key", "pointer", or "none", return an error
        if (type == null) {
            throw new InvalidArgumentException("'type' is required in touch action and must be one of: pointer, key, none");
        }

        // 3: Get the ID from the input source
        String id = inputSource.getId();

        // 4: If id is undefined or is not a String, return error
        if (id == null) {
            throw new InvalidArgumentException("'id' in touch action cannot be null");
        }

        // 5: Skip 'process pointer parameters'. This is already covered by deserializer

        // 6: Let source be the input source in the list of active input sources where that input source’s input id matches id,
        InputSource activeSource = activeInputSources.getInputSource(inputSource);

        // 7:  source is undefined:
        if (activeSource == null) {
            activeInputSources.addInputSource(inputSource);

            activeSource = inputSource;
        } else {

            // 8: If source's source type does not match type return an error
            if (activeSource.getType() != inputSource.getType()) {
                throw new InvalidArgumentException(String.format("Input type %s does not match pre-existing input type '%s' in actions input source with id '%s'",
                        inputSource.getType(), activeSource.getType(),  id));
            }

            // 9: If it's a pointer type, check that they match
            if (activeSource.getType() == InputSourceType.POINTER) {
                if (activeSource.getPointerType() != inputSource.getPointerType()) {
                    throw new InvalidArgumentException(String.format("Pointer type %s does not match pre-existing pointer type '%s' in actions input source with id '%s'",
                            inputSource.getPointerType(), activeSource.getPointerType(),id));
                }
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
                    //actionObjects.add(processPointerAction(action));
                    break;
                case KEY:
                    //actionObjects.add(processKeyAction(action));
                    break;
                default:
                    break;
            }
        }
    }

    /*public void processSourceActionSequence(InputSource inputSource) throws InvalidArgumentException {
        ActiveInputSources activeInputSources = ActiveInputSources.getInstance();
        processSourceActionSequence(inputSource, activeInputSources);
    }*/

    /**
     * Implement the 'process a null action' in 17.3
     * @param action
     * @param inputSourceType
     * @param id
     * @param index
     * @return
     * @throws InvalidArgumentException
     */
    public static ActionObject processNullAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        if (action.getType() != PAUSE) {
            throw new InvalidArgumentException(String.format("null action in actions[%s] of action input source with id '%s' must only have type 'pause'",
                    index, id));
        }
        return processPauseAction(action, inputSourceType, id, index);
    }

    /**
     * Follows the 'process a pointer action' algorithm in 17.2
     * @param action
     * @param inputSourceType
     * @param id
     * @param index
     * @return
     * @throws InvalidArgumentException
     */
    public static ActionObject processPointerAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        return null;
    }

    /**
     * Follows the 'process a key action' algorithm in 17.2
     * @param action
     * @param inputSourceType
     * @param id
     * @param index
     * @return
     * @throws InvalidArgumentException
     */
    public static ActionObject processKeyAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        return null;
    }

    /**
     * Follows the 'process a pause action' algorithm in 17.3
     * @param action
     * @param inputSourceType
     * @param id
     * @param index
     * @return
     * @throws InvalidArgumentException
     */
    public static ActionObject processPauseAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        Long duration = action.getDuration();
        assertNullOrPositive(index, id, "duration", duration);
        ActionObject actionObject = new ActionObject(id, inputSourceType, action, index);
        actionObject.setDuration(duration);
        return actionObject;
    }

    /**
     * Follows the 'process a pointer up or pointer down action' algorithm in 17.2
     * @param action
     * @param inputSourceType
     * @param id
     * @param index
     * @return
     * @throws InvalidArgumentException
     */
    public static ActionObject processPointerUpOrDownAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        ActionObject actionObject = new ActionObject(id, inputSourceType, action, index);
        int button = action.getButton();
        if (button < 0) {
            throwArgException(index, id, String.format("property 'button' must be greater than or equal to 0. Found %s", button));
        }
        actionObject.setButton(button);
        return actionObject;
    }

    /**
     * Follows the 'process pointer move action' algorithm in 17.3
     * @param action
     * @param inputSourceType
     * @param id
     * @param index
     * @return
     * @throws InvalidArgumentException
     */
    public static ActionObject processPointerMoveAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        ActionObject actionObject = new ActionObject(id, inputSourceType, action, index);

        // 1-3 Add the duration
        Long duration = action.getDuration();
        assertNullOrPositive(index, id, "duration", duration);
        actionObject.setDuration(duration);

        // 4-7 Add the origin
        String origin = action.getOrigin();
        if (origin == null) {
            origin = VIEWPORT;
        }
        actionObject.setOrigin(origin);

        // 8-10 Add the X coordinate
        Long x = action.getX();
        assertNullOrPositive(index, id, "x", x);
        actionObject.setX(x);

        // 11-14 Add the Y coordinate
        Long y = action.getY();
        assertNullOrPositive(index, id, "y", y);
        actionObject.setY(y);

        return actionObject;
    }

    private static boolean isNullOrPositive(Long num) {
        return num == null || num >= 0;
    }

    private static void throwArgException(int index, String id, String message) throws InvalidArgumentException {
        throw new InvalidArgumentException(String.format("pointer move action in actions[%s] of action input source with id %s %s",
                index, id, message));
    }

    private static void assertNullOrPositive(int index, String id, String propertyName, Long propertyValue) throws InvalidArgumentException {
        if (!isNullOrPositive(propertyValue)) {
            throwArgException(index, id, String.format(
                    "must have property '%s' be greater than or equal to 0 or undefined. Found %s", propertyName, propertyValue)
            );
        }
    }
}
