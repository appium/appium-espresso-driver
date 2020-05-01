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
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable

import io.appium.espressoserver.lib.helpers.w3c.processor.KeyProcessor.processKeyAction
import io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processNullAction
import io.appium.espressoserver.lib.helpers.w3c.processor.PointerProcessor.processPointerAction

object ActionsProcessor {

    /**
     * Follows algorithm "for process an input source action sequence" in section 17.3
     */
    @Throws(InvalidArgumentException::class, NotYetImplementedException::class)
    fun processSourceActionSequence(inputSource: InputSource, activeInputSources: ActiveInputSources, inputStateTable: InputStateTable): List<ActionObject> {
        // 1: Get the type
        // 2: If type is not "key", "pointer", or "none", return an error
        inputSource.type
                ?: throw InvalidArgumentException("'type' is required in input source and must be one of: pointer, key, none")


        // 3: Get the ID from the input source
        // 4: If id is undefined or is not a String, return error
        val id = inputSource.id
                ?: throw InvalidArgumentException("'id' in action cannot be null")


        // 6: Let source be the input source in the list of active input sources where that input source’s input id matches id,
        val activeSource = activeInputSources.getInputSource(inputSource)

        // 7:  source is undefined:
        if (activeSource == null) {
            activeInputSources.addInputSource(inputSource)
            inputStateTable.addInputState(inputSource.id!!, inputSource.defaultState!!)
        } else {

            // 8: If source's type does not match type return an error
            if (activeSource.type != inputSource.type) {
                throw InvalidArgumentException("Input type ${inputSource.type} does not match pre-existing input type '${activeSource.type}' in actions input source with id '${id}'")
            }

            // 9: If it's a pointer type, check that they match parameter types
            if (activeSource.type == InputSourceType.POINTER && activeSource.pointerType != inputSource.pointerType) {
                throw InvalidArgumentException("Pointer type ${inputSource.pointerType} does not match pre-existing pointer type '${activeSource.pointerType}' in actions input " +
                        "source with id '${id}'")
            }
        }

        // 10: Let action items be the result of getting a property named actions from action sequence
        // 11: If action items is not an Array, return error
        val actionItems = inputSource.actions
                ?: throw InvalidArgumentException("'actions' array not provided in actions input source with id '${id}'")

        // 12: Let actions be a new list
        val actionObjects = arrayListOf<ActionObject>()

        // 13: For each item in action items
        var index = 0
        for (action in actionItems) {
            index++
            when (inputSource.type) {
                InputSourceType.NONE -> actionObjects.add(processNullAction(action, inputSource.type, id, index))
                InputSourceType.POINTER -> actionObjects.add(processPointerAction(action, inputSource, id, index))
                InputSourceType.KEY -> actionObjects.add(processKeyAction(action, inputSource.type, id, index))
                else -> throw InvalidArgumentException("'actions[${index}]' of input source with id '${id}' did not provide a valid type")
            }
        }

        return actionObjects
    }
}
