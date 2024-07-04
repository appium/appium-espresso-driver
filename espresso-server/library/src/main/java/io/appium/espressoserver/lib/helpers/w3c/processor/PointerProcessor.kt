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
package io.appium.espressoserver.lib.helpers.w3c.processor

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processPauseAction

object PointerProcessor {
    /**
     * Follows the 'process a pointer action' algorithm in 17.2
     * @param action Action being processed
     * @param inputSource Input source
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    @Throws(InvalidArgumentException::class)
    fun processPointerAction(action: InputSource.Action, inputSource: InputSource, id: String?, index: Int): ActionObject {

        // 1 -2 get and validate the type
        val subType = action.type
        val validKeyTypes = listOf(InputSource.ActionType.POINTER_MOVE, InputSource.ActionType.POINTER_DOWN,
                InputSource.ActionType.POINTER_UP, InputSource.ActionType.POINTER_CANCEL, InputSource.ActionType.PAUSE)
        if (!validKeyTypes.contains(subType)) {
            throwArgException(index, id, "has an invalid type. 'type' for 'key' actions must be one of:" +
                    "pointerMove, pointerDown, pointerUp, pointerCancel, pause")
        }
        val actionObject: ActionObject

        // 4 if pause return PAUSE action
        if (subType == InputSource.ActionType.PAUSE) {
            return processPauseAction(action, inputSource.type, id, index)
        }
        actionObject = when (subType) {
            InputSource.ActionType.POINTER_DOWN, InputSource.ActionType.POINTER_UP -> processPointerUpOrDownAction(action, inputSource.type, id, index)
            InputSource.ActionType.POINTER_MOVE -> processPointerMoveAction(action, inputSource.type, id, index)
            InputSource.ActionType.POINTER_CANCEL -> processPointerCancelAction(action, inputSource.type, id, index)
            else -> throw InvalidArgumentException(String.format("Invalid pointer type %s", subType))
        }
        actionObject.pointer = inputSource.pointerType
        return actionObject
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
    @Throws(InvalidArgumentException::class)
    fun processPointerUpOrDownAction(action: InputSource.Action, inputSourceType: InputSourceType?, id: String?, index: Int): ActionObject {
        val actionObject = ActionObject(id, inputSourceType, action.type, index)
        val button = action.button
        if (button!! < 0) {
            throwArgException(index, id, String.format("property 'button' must be greater than or equal to 0. Found %s", button))
        }
        actionObject.button = button
        return actionObject
    }

    fun processPointerCancelAction(action: InputSource.Action, inputSourceType: InputSourceType?, id: String?, index: Int): ActionObject {
        return ActionObject(id, inputSourceType, action.type, index)
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
    @Throws(InvalidArgumentException::class)
    fun processPointerMoveAction(action: InputSource.Action, inputSourceType: InputSourceType?, id: String?, index: Int): ActionObject {
        val actionObject = ActionObject(id, inputSourceType, action.type, index)

        // 1-3 Add the duration
        val duration = action.duration
        assertNullOrPositive(index, id, "duration", duration)
        actionObject.duration = duration

        // 4-7 Add the origin
        actionObject.origin = action.origin

        // 8-10 Add the X coordinate
        val x = action.x
        actionObject.x = x

        // 11-14 Add the Y coordinate
        val y = action.y
        actionObject.y = y
        return actionObject
    }
}