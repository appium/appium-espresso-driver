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

object PauseProcessor {
    /**
     * Implement the 'process a null action' in 17.3
     * @param action Action being processed
     * @param inputSourceType Source type
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    @Throws(InvalidArgumentException::class)
    fun processNullAction(action: InputSource.Action, inputSourceType: InputSourceType?, id: String?, index: Int): ActionObject {
        if (action.type != InputSource.ActionType.PAUSE) {
            throwArgException(index, id, "must be type 'pause' if input source type is null")
        }
        return processPauseAction(action, inputSourceType, id, index)
    }

    /**
     * Follows the 'process a pause action' algorithm in 17.3
     * @param action Action being processed
     * @param inputSourceType Source type
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    @JvmStatic
    @Throws(InvalidArgumentException::class)
    fun processPauseAction(action: InputSource.Action, inputSourceType: InputSourceType?, id: String?, index: Int): ActionObject {
        val duration = action.duration
        assertNullOrPositive(index, id, "duration", duration)
        val actionObject = ActionObject(id, inputSourceType, InputSource.ActionType.PAUSE, index)
        actionObject.duration = duration
        return actionObject
    }
}