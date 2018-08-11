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
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_CANCEL;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_UP;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processPauseAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.ProcessorHelpers.assertNullOrPositive;
import static io.appium.espressoserver.lib.helpers.w3c.processor.ProcessorHelpers.throwArgException;

@SuppressWarnings("unused")
public class PointerProcessor {

    /**
     * Follows the 'process a pointer action' algorithm in 17.2
     * @param action Action being processed
     * @param inputSource Input source
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    public static ActionObject processPointerAction(Action action, InputSource inputSource, String id, int index)
            throws InvalidArgumentException, NotYetImplementedException {

        // 1 -2 get and validate the type
        ActionType subType = action.getType();
        ActionType[] validKeyTypes = new ActionType[]{ POINTER_MOVE, POINTER_DOWN, POINTER_UP, POINTER_CANCEL, PAUSE };
        if (!Arrays.asList(validKeyTypes).contains(subType)) {
            throwArgException(index, id, "has an invalid type. 'type' for 'key' actions must be one of:" +
                    "pointerMove, pointerDown, pointerUp, pointerCancel, pause");
        }

        ActionObject actionObject;

        // 4 if pause return PAUSE action
        if (subType.equals(PAUSE)) {
            return processPauseAction(action, inputSource.getType(), id, index);
        }

        // 5-8 check type and return proper action object
        switch (subType) {
            case POINTER_DOWN:
            case POINTER_UP:
                actionObject = processPointerUpOrDownAction(action, inputSource.getType(), id, index);
                break;
            case POINTER_MOVE:
                actionObject = processPointerMoveAction(action, inputSource.getType(), id, index);
                break;
            case POINTER_CANCEL:
                actionObject = processPointerCancelAction(action, inputSource.getType(), id, index);
                break;
            default:
                // Technically unreachable because the 'validKeyTypes' check catches this
                throw new InvalidArgumentException(String.format("Invalid pointer type %s", subType));
        }

        actionObject.setPointer(inputSource.getPointerType());
        return actionObject;
    }

    /**
     * Follows the 'process a pointer up or pointer down action' algorithm in 17.2
     * @param action Action being processed
     * @param inputSourceType Source type
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    public static ActionObject processPointerUpOrDownAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        ActionObject actionObject = new ActionObject(id, inputSourceType, action.getType(), index);
        int button = action.getButton();
        if (button < 0) {
            throwArgException(index, id, String.format("property 'button' must be greater than or equal to 0. Found %s", button));
        }
        actionObject.setButton(button);
        return actionObject;
    }

    public static ActionObject processPointerCancelAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        return new ActionObject(id, inputSourceType, action.getType(), index);
    }

    /**
     * Follows the 'process pointer move action' algorithm in 17.3
     * @param action Action being processed
     * @param inputSourceType Source type
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    public static ActionObject processPointerMoveAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        ActionObject actionObject = new ActionObject(id, inputSourceType, action.getType(), index);

        // 1-3 Add the duration
        Long duration = action.getDuration();
        assertNullOrPositive(index, id, "duration", duration);
        actionObject.setDuration(duration);

        // 4-7 Add the origin
        actionObject.setOrigin(action.getOrigin());

        // 8-10 Add the X coordinate
        Long x = action.getX();
        actionObject.setX(x);

        // 11-14 Add the Y coordinate
        Long y = action.getY();
        actionObject.setY(y);

        return actionObject;
    }
}
