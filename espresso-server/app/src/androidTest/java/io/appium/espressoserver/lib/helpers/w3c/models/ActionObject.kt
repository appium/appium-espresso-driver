package io.appium.espressoserver.lib.helpers.w3c.models

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.dispatchKeyDown
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.dispatchKeyUp
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState
import java.util.concurrent.Callable

class ActionObject {
    private var index = 0
    var type: InputSourceType? = null
    var subType: InputSource.ActionType? = null
    var id: String? = null
    var duration: Float? = null
    var x: Float? = null
        get() { return field ?: 0f }
    var y: Float? = null
        get() { return field ?: 0f }
    var button = 0
    var value: String? = null
    var pointer: InputSource.PointerType? = null
    var origin = Origin()

    constructor() {}

    // Copy constructor
    constructor(actionObject: ActionObject) {
        index = actionObject.index
        type = actionObject.type
        subType = actionObject.subType
        id = actionObject.id
        duration = actionObject.duration
        origin = actionObject.origin
        x = actionObject.x
        y = actionObject.y
        button = actionObject.button
        value = actionObject.value
        pointer = actionObject.pointer
    }

    constructor(id: String?, type: InputSourceType?, subType: InputSource.ActionType?, index: Int) {
        this.type = type
        this.subType = subType
        this.id = id
        this.index = index // Store the index of the action for possible future logging issues
    }

    /**
     * Call `dispatch tick actions` algorithm in section 17.4
     * @param adapter Adapter for actions
     * @param inputStateTable State of all inputs
     * @param tickDuration How long the tick is
     * @param timeAtBeginningOfTick When the tick began
     * @return
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    fun dispatch(adapter: W3CActionAdapter,
                 inputStateTable: InputStateTable,
                 tickDuration: Float, timeAtBeginningOfTick: Long): Callable<BaseDispatchResult>? {
        val inputSourceType = type
        val actionType = subType
        adapter.logger.info(String.format(
                "Dispatching action #%s of input source %s",
                index, id
        ))
        // 1.3 If the current session's input state table doesn't have a property corresponding to
        //      source id, then let the property corresponding to source id be a new object of the
        //      corresponding input source state type for source type.
        // 1.4 Let device state be the input source state corresponding to source id in the current session’s input state table
        val deviceState = inputStateTable.getOrCreateInputState(id!!, this)
        try {
            if (inputSourceType == InputSourceType.KEY) {
                when (actionType) {
                    InputSource.ActionType.KEY_DOWN -> dispatchKeyDown(adapter, this, (deviceState as KeyInputState), inputStateTable, tickDuration)
                    InputSource.ActionType.KEY_UP -> dispatchKeyUp(adapter, this, (deviceState as KeyInputState), inputStateTable, tickDuration)
                    else -> {}
                }
            } else if (inputSourceType == InputSourceType.POINTER) {
                when (actionType) {
                    InputSource.ActionType.POINTER_MOVE -> return PointerDispatch.dispatchPointerMove(
                            adapter,
                            id,
                            this,
                            deviceState as PointerInputState,
                            tickDuration,
                            timeAtBeginningOfTick,
                            inputStateTable.globalKeyInputState
                    )
                    InputSource.ActionType.POINTER_DOWN -> {
                        PointerDispatch.dispatchPointerDown(
                                adapter,
                                this,
                                deviceState as PointerInputState,
                                inputStateTable,
                                inputStateTable.globalKeyInputState
                        )
                        return null
                    }
                    InputSource.ActionType.POINTER_UP -> {
                        PointerDispatch.dispatchPointerUp(
                                adapter,
                                this,
                                deviceState as PointerInputState,
                                inputStateTable,
                                inputStateTable.globalKeyInputState
                        )
                        return null
                    }
                    InputSource.ActionType.POINTER_CANCEL -> {
                        PointerDispatch.dispatchPointerCancel(
                                adapter,
                                this
                        )
                        return null
                    }
                    else -> adapter.logger.info(String.format(
                            "Dispatching pause event for %s milliseconds",
                            duration
                    ))
                }
            }
        } catch (cce: ClassCastException) {
            throw InvalidArgumentException(String.format(
                    "Attempted to apply action of type '%s' to a source with type '%s': %s",
                    inputSourceType, deviceState!!.javaClass.simpleName, cce.message
            ))
        }
        return null
    }
}