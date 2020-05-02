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

object KeyProcessor {
    /**
     * Follows the 'process a key action' algorithm in 17.2
     * @param action Action being processed
     * @param inputSourceType Source type
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    @Throws(InvalidArgumentException::class)
    fun processKeyAction(action: InputSource.Action, inputSourceType: InputSourceType?, id: String?, index: Int): ActionObject {

        // 1-3 get and validate the action type
        val subType = action.type
        val validKeyTypes = listOf(InputSource.ActionType.KEY_UP, InputSource.ActionType.KEY_DOWN, InputSource.ActionType.PAUSE)
        if (!validKeyTypes.contains(subType)) {
            throwArgException(index, id, "has an invalid type. 'type' for 'key' actions must be one of: keyUp, keyDown or pause")
        }

        // 4 if pause return PAUSE action
        if (subType == InputSource.ActionType.PAUSE) {
            return PauseProcessor.processPauseAction(action, inputSourceType, id, index)
        }

        // 5-7 get the Unicode value of the keystroke (verify that it's a single character)
        val key = action.value
        if (key?.length != 1) {
            throwArgException(index, id, String.format("has invalid 'value' %s. Must be a unicode point", key))
        }
        val actionObject = ActionObject(id, inputSourceType, subType, index)
        actionObject.value = key
        return actionObject
    }
}