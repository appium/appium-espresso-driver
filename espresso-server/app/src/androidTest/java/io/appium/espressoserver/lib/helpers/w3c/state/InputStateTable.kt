package io.appium.espressoserver.lib.helpers.w3c.state

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import java.util.*

/**
 * Keep the state of all active input sources
 *
 * (defined in 17.1 of spec)
 */
class InputStateTable {
    private val stateTable: MutableMap<String, InputState> = HashMap()
    private val cancelList: MutableList<ActionObject?> = ArrayList()
    fun addInputState(id: String, inputState: InputState) {
        stateTable[id] = inputState
    }

    fun getInputState(id: String): InputState? {
        return stateTable[id]
    }

    fun getOrCreateInputState(sourceId: String, actionObject: ActionObject): InputState? {
        if (!hasInputState(sourceId)) {
            var newInputState: InputState? = null
            when (actionObject.type) {
                InputSourceType.KEY -> newInputState = KeyInputState()
                InputSourceType.POINTER -> newInputState = PointerInputState(actionObject.pointer)
                InputSourceType.NONE -> {
                }
                else -> {
                }
            }
            if (newInputState != null) {
                addInputState(sourceId, newInputState)
            }
        }
        return stateTable[sourceId]
    }

    fun hasInputState(id: String): Boolean {
        return stateTable.containsKey(id)
    }

    fun addActionToCancel(actionObject: ActionObject?) {
        cancelList.add(actionObject)
    }

    fun getCancelList(): List<ActionObject> {
        return cancelList.filterNotNull()
    }

    val globalKeyInputState: KeyInputState
        get() {
            val keyInputStates: MutableList<KeyInputState> = ArrayList()
            for ((_, inputState) in stateTable) {
                if (inputState.javaClass == KeyInputState::class.java) {
                    keyInputStates.add(inputState as KeyInputState)
                }
            }
            return KeyInputState.getGlobalKeyState(keyInputStates)
        }

    /**
     * Do the release actions to undo everything
     * @param adapter W3C Action adapter
     * @param timeAtBeginningOfTick When did the tick begin
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    fun undoAll(adapter: W3CActionAdapter, timeAtBeginningOfTick: Long) {
        // 2-3: Dispatch tick actions with arguments undo actions and duration 0 in reverse order
        Collections.reverse(cancelList)
        for (actionObject in cancelList) {
            actionObject!!.dispatch(adapter, this, 0f, timeAtBeginningOfTick)
        }
        adapter.sychronousTickActionsComplete()

        // Clear the cancel list now that the Undo operations are all fulfilled
        cancelList.clear()
    }

    companion object {
        private val inputStateTables: MutableMap<String, InputStateTable> = HashMap()

        /**
         * Get the global input states for a given session
         * @param sessionId ID of the session
         * @return
         */
        @Synchronized
        fun getInputStateTableOfSession(sessionId: String): InputStateTable? {
            var globalInputStateTable = inputStateTables[sessionId]
            if (globalInputStateTable == null) {
                inputStateTables[sessionId] = InputStateTable()
                globalInputStateTable = inputStateTables[sessionId]
            }
            return globalInputStateTable
        }
    }
}