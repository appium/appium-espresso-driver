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

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE;
import static io.appium.espressoserver.lib.helpers.w3c.processor.ProcessorHelpers.assertNullOrPositive;
import static io.appium.espressoserver.lib.helpers.w3c.processor.ProcessorHelpers.throwArgException;

@SuppressWarnings("unused")
public class PauseProcessor {

    /**
     * Implement the 'process a null action' in 17.3
     * @param action Action being processed
     * @param inputSourceType Source type
     * @param id ID of input source that it's part of
     * @param index Index within the 'actions' array
     * @return Processed action object
     * @throws InvalidArgumentException If failed to process, throw this. Means that args are bad.
     */
    public static ActionObject processNullAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        if (action.getType() != PAUSE) {
            throwArgException(index, id, "must be type 'pause' if input source type is null");
        }
        return processPauseAction(action, inputSourceType, id, index);
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
    public static ActionObject processPauseAction(Action action, InputSourceType inputSourceType, String id, int index) throws InvalidArgumentException {
        Long duration = action.getDuration();
        assertNullOrPositive(index, id, "duration", duration);
        ActionObject actionObject = new ActionObject(id, inputSourceType, PAUSE, index);
        actionObject.setDuration(duration);
        return actionObject;
    }
}
