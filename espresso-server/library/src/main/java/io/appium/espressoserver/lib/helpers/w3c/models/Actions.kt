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
package io.appium.espressoserver.lib.helpers.w3c.models

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.model.AppiumParams
import java.util.concurrent.ExecutionException

class Actions : AppiumParams() {
    var actions: List<InputSource>? = null
    var adapter: W3CActionAdapter? = null

    /**
     * Perform actions (17.5)
     * @param sessionId ID of the session to perform actions on
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    fun perform(sessionId: String) {
        if (adapter == null) {
            throw AppiumException("An internal server error has occurred: Failed to initialize /actions adapter")
        }

        // Get state of session
        val activeInputSources = ActiveInputSources.getActiveInputSourcesForSession(sessionId)
        val inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId)

        // Let `actions by tick` be the result of trying to extract an action sequence with argument parameters
        adapter!!.logger.info("Performing actions")
        val actionsByTick = ActionSequence(this, activeInputSources, inputStateTable)
        try {
            // Dispatch the actions
            actionsByTick.dispatch(adapter!!, inputStateTable)
        } catch (e: InterruptedException) {
            throw AppiumException(e.cause.toString())
        } catch (e: ExecutionException) {
            throw AppiumException(e.cause.toString())
        }
    }

    /**
     * Release actions (17.6)
     * @param sessionId ID of the session to release actions on
     */
    @Throws(AppiumException::class)
    fun release(sessionId: String) {
        if (adapter == null) {
            throw AppiumException("An internal server error has occurred: Failed to initialize /actions adapter")
        }

        // Get state of session
        val inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId)

        // Undo all actions
        adapter!!.logger.info(String.format("Releasing actions performed during session %s", sessionId))
        inputStateTable.undoAll(adapter!!, System.currentTimeMillis())
    }

    class ActionsBuilder {
        private var actions: List<InputSource>? = null
        private var adapter: W3CActionAdapter? = null
        fun withActions(actions: List<InputSource>?): ActionsBuilder = apply { this.actions = actions }

        fun withAdapter(adapter: W3CActionAdapter?): ActionsBuilder = apply { this.adapter = adapter }

        fun build(): Actions {
            val actions = Actions()
            actions.adapter = adapter
            actions.actions = this.actions
            return actions
        }
    }
}