package io.appium.espressoserver.lib.helpers.w3c.models

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import java.util.*
import java.util.concurrent.Callable

class Tick : Iterator<ActionObject> {
    private val tickActions: MutableList<ActionObject> = ArrayList()
    private var actionCounter = 0
    fun addAction(action: ActionObject) {
        tickActions.add(action)
    }

    override fun hasNext(): Boolean {
        return actionCounter < tickActions.size
    }

    override fun next(): ActionObject {
        return tickActions[actionCounter++]
    }

    /**
     * Get max tick duration for a tick
     * @return Max tick duration
     */
    fun calculateTickDuration(): Float {
        var maxDuration = 0f
        for (actionObject in tickActions) {
            var currDuration = 0.0f
            val type = actionObject.type
            val duration = actionObject.duration
            val subType = actionObject.subType
            if (duration != null && (subType === InputSource.ActionType.PAUSE
                            || type === InputSourceType.POINTER && subType === InputSource.ActionType.POINTER_MOVE)) {
                currDuration = duration
            }
            if (currDuration > maxDuration) {
                maxDuration = currDuration
            }
        }
        return maxDuration
    }

    @Throws(AppiumException::class)
    fun dispatchAll(adapter: W3CActionAdapter, inputStateTable: InputStateTable, tickDuration: Float): List<Callable<BaseDispatchResult>> {
        val timeAtBeginningOfTick = System.currentTimeMillis()
        val asyncOperations: MutableList<Callable<BaseDispatchResult>> = ArrayList()
        for (actionObject in tickActions) {
            // 2. Run algorithm with arguments source id, action object, device state and tick duration
            val dispatchResult = actionObject.dispatch(adapter,
                    inputStateTable, tickDuration, timeAtBeginningOfTick)

            // If it's an async operation, add it to the list
            dispatchResult?.let { asyncOperations.add(it) }
        }
        return asyncOperations
    }
}