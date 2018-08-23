package io.appium.espressoserver.lib.helpers.w3c.dispatcher;

import android.graphics.Point;

import java.util.concurrent.Callable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.Origin;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ELEMENT;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.POINTER;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.VIEWPORT;

//import io.appium.espressoserver.lib.helpers.AndroidLogger;

public class PointerDispatch {

    private static void dispatchPointerEvent(final W3CActionAdapter dispatcherAdapter,
                                             final ActionObject actionObject,
                                             final PointerInputState pointerInputState,
                                             final InputStateTable inputStateTable,
                                             final KeyInputState globalKeyInputState,
                                             final boolean down) throws AppiumException {
        PointerType pointerType = actionObject.getPointer();
        int button = actionObject.getButton();

        if (down) {
            if (pointerInputState.isPressed(button)) {
                return;
            }
        } else {
            if (!pointerInputState.isPressed(button)) {
                return;
            }
        }
        Long x = pointerInputState.getX();
        Long y = pointerInputState.getY();
        if (down) {
            pointerInputState.addPressed(button);
        } else {
            pointerInputState.removePressed(button);
        }

        dispatcherAdapter.getLogger().info(String.format(
                "Dispatching pointer event '%s' on input source with id '%s' with coordinates [%s, %s] " +
                "and button '%s'",
                down ? "pointerDown": "pointerUp", actionObject.getId(), x, y, button
        ));

        dispatcherAdapter.lockAdapter();
        try {
            if (down) {
                // Add cancel object to cancel list
                ActionObject cancelObject = new ActionObject(actionObject);
                cancelObject.setSubType(POINTER_UP);
                inputStateTable.addActionToCancel(cancelObject);

                // Dispatch implementation specific pointer down
                dispatcherAdapter.pointerDown(button, actionObject.getId(), pointerType, x, y,
                        pointerInputState.getButtons(), globalKeyInputState);
            } else {
                // Dispatch implementation specific pointer up
                dispatcherAdapter.pointerUp(button, actionObject.getId(), pointerType, x, y,
                        pointerInputState.getButtons(), globalKeyInputState);
            }
        } finally {
            dispatcherAdapter.unlockAdapter();
        }

        // Log the new state of the pointer
        dispatcherAdapter.getLogger().info(String.format(
                "State of pointer input source with id %s is now: %s",
                actionObject.getId(), pointerInputState.logMessage()
        ));
    }

    /**
     * Run the 'dispatch a pointer down' algorithm
     * @param dispatcherAdapter W3C actions implementation
     * @param actionObject Action object that defines the pointer action
     * @param pointerInputState Current state of the input source
     * @param globalKeyInputState Global key input state
     * @throws AppiumException
     */
    public static void dispatchPointerDown(final W3CActionAdapter dispatcherAdapter,
                                           final ActionObject actionObject,
                                           final PointerInputState pointerInputState,
                                           final InputStateTable inputStateTable,
                                           final KeyInputState globalKeyInputState) throws AppiumException {
        dispatchPointerEvent(dispatcherAdapter, actionObject, pointerInputState,
                inputStateTable, globalKeyInputState, true);
    }

    /**
     * Perform the 'dispatch pointer up' algorithm
     * @param dispatcherAdapter W3C actions implementation
     * @param actionObject Action object that defines the pointer action
     * @param pointerInputState Current state of the input source
     * @param globalKeyInputState Global key input state
     * @throws AppiumException
     */
    public static void dispatchPointerUp(final W3CActionAdapter dispatcherAdapter,
                                           final ActionObject actionObject,
                                           final PointerInputState pointerInputState,
                                            final InputStateTable inputStateTable,
                                           final KeyInputState globalKeyInputState) throws AppiumException {
        dispatchPointerEvent(dispatcherAdapter, actionObject, pointerInputState,
                inputStateTable, globalKeyInputState, false);
    }

    /**
     * Perform the 'dispatch a pointer cancel' event
     * @param dispatcherAdapter ActionsPerformer adapter
     * @param actionObject Action object
     * @throws AppiumException
     */
    public static void dispatchPointerCancel(final W3CActionAdapter dispatcherAdapter,
                                             final ActionObject actionObject) throws AppiumException {
        dispatcherAdapter.pointerCancel(actionObject.getId(), actionObject.getPointer());
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
    public static Callable<BaseDispatchResult> dispatchPointerMove(final W3CActionAdapter dispatcherAdapter,
                                                     final String sourceId,
                                                     final ActionObject actionObject,
                                                     final PointerInputState pointerInputState,
                                                     final long tickDuration,
                                                     final long timeAtBeginningOfTick,
                                                     final KeyInputState globalKeyInputState) throws AppiumException {
        // 1.5 Variable definitions
        long xOffset = actionObject.getX();
        long yOffset = actionObject.getY();
        long startX = pointerInputState.getX();
        long startY = pointerInputState.getY();
        Origin origin = actionObject.getOrigin();
        
        dispatcherAdapter.getLogger().info(String.format(
            "Dispatching pointer move '%s' on input source with id '%s' with origin '%s' and coordinates [%s, %s]",
            pointerInputState.getType(), sourceId, origin, xOffset, yOffset
        ));

        long x;
        long y;

        // 6. Run the substeps of the first matching value of origin
        String originType = origin.getType();
        if (originType == null) {
            originType = VIEWPORT;
        }
        dispatcherAdapter.getLogger().info("Origin type is: ", originType);
        switch (origin.getType()) {
            case POINTER:
                x = startX + xOffset;
                y = startY + yOffset;
                break;
            case ELEMENT:
                dispatcherAdapter.getLogger().info("Getting element center point: ", origin.getElementId());
                final Point elementCoordinates = dispatcherAdapter.getElementCenterPoint(origin.getElementId());
                dispatcherAdapter.getLogger().info(String.format(
                        "Element center is: %s %s",
                        elementCoordinates.x, elementCoordinates.y
                ));
                x = elementCoordinates.x + xOffset;
                y = elementCoordinates.y + yOffset;
                break;
            case VIEWPORT:
                x = xOffset;
                y = yOffset;
                break;
            default:
                throw new InvalidArgumentException(String.format("'%s' is not a valid origin type", originType));
        }

        // 7-8. Bounds check
        if (x < 0 || y < 0 || x > dispatcherAdapter.getViewportWidth() || y > dispatcherAdapter.getViewportHeight()) {
            throw new MoveTargetOutOfBoundsException(x, y);
        }

        // 9. Let duration be equal to action object's duration property if it is not undefined, or tick duration otherwise
        Long duration = actionObject.getDuration();
        if (duration == null) {
            duration = tickDuration;
        }

        Callable<BaseDispatchResult> callable = performPointerMove(
                dispatcherAdapter,
                sourceId,
                pointerInputState,
                duration,
                startX, startY,
                x, y,
                timeAtBeginningOfTick,
                globalKeyInputState
        );


        // Log the new state of the pointer
        dispatcherAdapter.getLogger().info(String.format(
                "State of pointer input source with id %s is now: %s",
               actionObject.getId(), pointerInputState.logMessage()
        ));

        return callable;
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
    public static Callable<BaseDispatchResult> performPointerMove(final W3CActionAdapter dispatcherAdapter,
                                                    final String sourceId,
                                                    final PointerInputState pointerInputState,
                                                    final long duration,
                                                    final long startX, final long startY,
                                                    final long targetX, final long targetY,
                                                    final long timeAtBeginningOfTick,
                                                    final KeyInputState globalKeyInputState) {
        return new Callable<BaseDispatchResult>() {
            @Override
            public BaseDispatchResult call() throws Exception {
                boolean isLast;

                // 2. Let time delta be the time since the beginning of the current tick, measured in milliseconds on a monotonic clock
                long timeDelta = System.currentTimeMillis() - timeAtBeginningOfTick;

                // 3. Let duration ratio be the ratio of time delta and duration, if duration is greater than 0, or 1 otherwise
                float durationRatio = duration > 0 ? timeDelta / ((float) duration) : 1;

                // 4. If duration ratio is 1, or close enough to 1 that the implementation will not further subdivide the move action,
                //    let last be true. Otherwise let last be false
                isLast = (1 - durationRatio) <= dispatcherAdapter.getPointerMoveDurationMargin(pointerInputState);

                // 5. If last is true, let x equal target x and y equal target y
                // 6. Otherwise let x equal an approximation to duration ratio × (target x - start x) + start x,, ...
                final long x = isLast ? targetX : Math.round(durationRatio * (targetX - startX)) + startX;
                final long y = isLast ? targetY : Math.round(durationRatio * (targetY - startY)) + startY;

                // 7-8: Let currentX and currentY be pointer input state
                final long currentX = pointerInputState.getX();
                final long currentY = pointerInputState.getY();

                // Prepare the result
                DispatchPointerMoveResult dispatchResult = new DispatchPointerMoveResult(
                        dispatcherAdapter,
                        sourceId,
                        pointerInputState.getType(),
                        currentX, currentY,
                        x, y,
                        pointerInputState.getButtons(),
                        globalKeyInputState
                );

                if (currentX != x || currentY != y) {

                    // 8.3. Let input state's x property equal x and y property equal y
                    pointerInputState.setX(x);
                    pointerInputState.setY(y);
                }

                // Sleep for a fixed period of time
                if (duration > 0) {
                    Thread.sleep(dispatcherAdapter.pointerMoveIntervalDuration());
                }

                if (!isLast) {
                    // 11. Perform a pointer move with arguments source id, input state, duration, start x, start y, target x, target y)
                    dispatchResult.setNext(
                            performPointerMove(
                                    dispatcherAdapter,
                                    sourceId,
                                    pointerInputState,
                                    duration,
                                    startX, startY,
                                    targetX, targetY,
                                    timeAtBeginningOfTick,
                                    globalKeyInputState
                            )
                    );
                }

                return dispatchResult;

            }
        };
    }
}
