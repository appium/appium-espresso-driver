package io.appium.espressoserver.lib.helpers.w3c.dispatcher

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException
import io.appium.espressoserver.lib.helpers.Rect
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.*
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState
import java.util.concurrent.Callable
import kotlin.math.roundToInt

object PointerDispatch {
    @Throws(AppiumException::class)
    private fun dispatchPointerEvent(dispatcherAdapter: W3CActionAdapter,
                                     actionObject: ActionObject,
                                     pointerInputState: PointerInputState,
                                     inputStateTable: InputStateTable,
                                     globalKeyInputState: KeyInputState?,
                                     down: Boolean) {
        val pointerType = actionObject.pointer
        val button = actionObject.button
        if (down) {
            if (pointerInputState.isPressed(button)) {
                return
            }
        } else {
            if (!pointerInputState.isPressed(button)) {
                return
            }
        }
        val x = pointerInputState.x
        val y = pointerInputState.y
        if (down) {
            pointerInputState.addPressed(button)
        } else {
            pointerInputState.removePressed(button)
        }
        dispatcherAdapter.logger.info(String.format(
                "Dispatching pointer event '%s' on input source with id '%s' with coordinates [%s, %s] " +
                        "and button '%s'",
                if (down) "pointerDown" else "pointerUp", actionObject.id, x, y, button
        ))
        dispatcherAdapter.lockAdapter()
        try {
            if (down) {
                // Add cancel object to cancel list
                val cancelObject = ActionObject(actionObject)
                cancelObject.subType = InputSource.ActionType.POINTER_UP
                inputStateTable.addActionToCancel(cancelObject)

                // Dispatch implementation specific pointer down
                dispatcherAdapter.pointerDown(button, actionObject.id!!, pointerType, x, y,
                        pointerInputState.buttons, globalKeyInputState)
            } else {
                // Dispatch implementation specific pointer up
                dispatcherAdapter.pointerUp(button, actionObject.id!!, pointerType, x, y,
                        pointerInputState.buttons, globalKeyInputState)
            }
        } finally {
            dispatcherAdapter.unlockAdapter()
        }

        // Log the new state of the pointer
        dispatcherAdapter.logger.info(String.format(
                "State of pointer input source with id %s is now: %s",
                actionObject.id, pointerInputState.logMessage()
        ))
    }

    /**
     * Run the 'dispatch a pointer down' algorithm
     * @param dispatcherAdapter W3C actions implementation
     * @param actionObject Action object that defines the pointer action
     * @param pointerInputState Current state of the input source
     * @param globalKeyInputState Global key input state
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    fun dispatchPointerDown(dispatcherAdapter: W3CActionAdapter,
                            actionObject: ActionObject,
                            pointerInputState: PointerInputState,
                            inputStateTable: InputStateTable,
                            globalKeyInputState: KeyInputState?) {
        dispatchPointerEvent(dispatcherAdapter, actionObject, pointerInputState,
                inputStateTable, globalKeyInputState, true)
    }

    /**
     * Perform the 'dispatch pointer up' algorithm
     * @param dispatcherAdapter W3C actions implementation
     * @param actionObject Action object that defines the pointer action
     * @param pointerInputState Current state of the input source
     * @param globalKeyInputState Global key input state
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    fun dispatchPointerUp(dispatcherAdapter: W3CActionAdapter,
                          actionObject: ActionObject,
                          pointerInputState: PointerInputState,
                          inputStateTable: InputStateTable,
                          globalKeyInputState: KeyInputState?) {
        dispatchPointerEvent(dispatcherAdapter, actionObject, pointerInputState,
                inputStateTable, globalKeyInputState, false)
    }

    /**
     * Perform the 'dispatch a pointer cancel' event
     * @param dispatcherAdapter ActionsPerformer adapter
     * @param actionObject Action object
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    fun dispatchPointerCancel(dispatcherAdapter: W3CActionAdapter,
                              actionObject: ActionObject) {
        dispatcherAdapter.pointerCancel(actionObject.id!!, actionObject.pointer!!)
    }

    /**
     * Call the 'dispatch a pointerMove action' algorithm
     * @param dispatcherAdapter W3C actions implementation
     * @param sourceId ID of the input source
     * @param actionObject Action object that defines the pointer action
     * @param pointerInputState Current state of the input source
     * @param tickDuration How long is this tick (in ms)
     * @param timeAtBeginningOfTick Time when the current tick started (in ms)
     * @param globalKeyInputState Global key input state
     * @return Returns a callable that returns when the pointer move is complete
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    fun dispatchPointerMove(dispatcherAdapter: W3CActionAdapter,
                            sourceId: String?,
                            actionObject: ActionObject,
                            pointerInputState: PointerInputState,
                            tickDuration: Float,
                            timeAtBeginningOfTick: Long,
                            globalKeyInputState: KeyInputState?): Callable<BaseDispatchResult> {
        // 1.5 Variable definitions
        val xOffset = actionObject.x!!
        val yOffset = actionObject.y!!
        val startX = pointerInputState.x
        val startY = pointerInputState.y
        val origin = actionObject.origin
        dispatcherAdapter.logger.info(String.format(
                "Dispatching pointer move '%s' on input source with id '%s' with origin '%s' and coordinates [%s, %s]",
                pointerInputState.type, sourceId, origin, xOffset, yOffset
        ))
        val x: Float
        val y: Float

        // 6. Run the substeps of the first matching value of origin
        val originType = origin.type ?: Origin.VIEWPORT
        dispatcherAdapter.logger.info("Origin type is: ", originType)
        when (origin.type) {
            Origin.POINTER -> {
                x = startX + xOffset
                y = startY + yOffset
            }
            Origin.ELEMENT -> {
                requireNotNull(origin.elementId, { "Element identifier must be present for origin type 'ELEMENT'" })
                dispatcherAdapter.logger.info("Getting element center point: ", origin.elementId!!)
                val elementCoordinates = dispatcherAdapter.getElementCenterPoint(origin.elementId)
                dispatcherAdapter.logger.info(String.format(
                        "Element center is: %s %s",
                        elementCoordinates.x, elementCoordinates.y
                ))
                x = elementCoordinates.x + xOffset
                y = elementCoordinates.y + yOffset
            }
            Origin.VIEWPORT -> {
                x = xOffset
                y = yOffset
            }
            else -> throw InvalidArgumentException(String.format("'%s' is not a valid origin type", originType))
        }

        // 7-8. Bounds check
        val boundingRect = Rect(0, 0, dispatcherAdapter.viewportWidth.toInt(), dispatcherAdapter.viewportHeight.toInt())
        if (!boundingRect.contains(x.toInt(), y.toInt())) {
            throw MoveTargetOutOfBoundsException(x, y, boundingRect)
        }

        // 9. Let duration be equal to action object's duration property if it is not undefined, or tick duration otherwise
        val duration = actionObject.duration ?: tickDuration
        val callable = performPointerMove(
                dispatcherAdapter,
                sourceId,
                pointerInputState,
                duration,
                startX, startY,
                x, y,
                timeAtBeginningOfTick,
                globalKeyInputState
        )


        // Log the new state of the pointer
        dispatcherAdapter.logger.info(String.format(
                "State of pointer input source with id %s is now: %s",
                actionObject.id, pointerInputState.logMessage()
        ))
        return callable
    }

    /**
     * Implements the 'perform a pointer move' algorithm in section 17.4.3
     * @param dispatcherAdapter W3C actions implementation
     * @param sourceId ID of the input source
     * @param pointerInputState Current state of the input source
     * @param duration How long is the pointer move
     * @param startX Starting x coordinate
     * @param startY Starting y coordinate
     * @param targetX Target x coordinate
     * @param targetY Target y coordinate
     */
    fun performPointerMove(dispatcherAdapter: W3CActionAdapter,
                           sourceId: String?,
                           pointerInputState: PointerInputState,
                           duration: Float,
                           startX: Float, startY: Float,
                           targetX: Float, targetY: Float,
                           timeAtBeginningOfTick: Long,
                           globalKeyInputState: KeyInputState?): Callable<BaseDispatchResult> {
        return Callable {
            val isLast: Boolean

            // 2. Let time delta be the time since the beginning of the current tick, measured in milliseconds on a monotonic clock
            val timeDelta = System.currentTimeMillis() - timeAtBeginningOfTick

            // 3. Let duration ratio be the ratio of time delta and duration, if duration is greater than 0, or 1 otherwise
            val durationRatio: Float = if (duration > 0f) timeDelta / duration else 1f

            // 4. If duration ratio is 1, or close enough to 1 that the implementation will not further subdivide the move action,
            //    let last be true. Otherwise let last be false
            isLast = 1 - durationRatio <= dispatcherAdapter.getPointerMoveDurationMargin(pointerInputState)

            // 5. If last is true, let x equal target x and y equal target y
            // 6. Otherwise let x equal an approximation to duration ratio × (target x - start x) + start x,, ...
            val x = if (isLast) targetX else (durationRatio * (targetX - startX)).roundToInt() + startX
            val y = if (isLast) targetY else (durationRatio * (targetY - startY)).roundToInt() + startY

            // 7-8: Let currentX and currentY be pointer input state
            val currentX = pointerInputState.x
            val currentY = pointerInputState.y

            // Prepare the result
            val dispatchResult = DispatchPointerMoveResult(
                    dispatcherAdapter,
                    sourceId!!,
                    pointerInputState.type!!,
                    currentX, currentY,
                    x, y,
                    pointerInputState.buttons,
                    globalKeyInputState
            )
            if (currentX != x || currentY != y) {

                // 8.3. Let input state's x property equal x and y property equal y
                pointerInputState.x = x
                pointerInputState.y = y
            }

            // Sleep for a fixed period of time
            if (duration > 0) {
                Thread.sleep(dispatcherAdapter.pointerMoveIntervalDuration().toLong())
            }
            if (!isLast) {
                // 11. Perform a pointer move with arguments source id, input state, duration, start x, start y, target x, target y)
                dispatchResult.next = performPointerMove(
                        dispatcherAdapter,
                        sourceId,
                        pointerInputState,
                        duration,
                        startX, startY,
                        targetX, targetY,
                        timeAtBeginningOfTick,
                        globalKeyInputState
                )
            }
            dispatchResult
        }
    }
}