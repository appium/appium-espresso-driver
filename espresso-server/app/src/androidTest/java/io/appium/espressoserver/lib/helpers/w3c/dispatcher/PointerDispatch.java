package io.appium.espressoserver.lib.helpers.w3c.dispatcher;

import java.util.concurrent.Callable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException;
//import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.*;

public class PointerDispatch {

    /**
     * Run the 'dispatch a pointer down' algorithm
     * @param dispatcherAdapter W3C actions implementation
     * @param sourceId ID of the input source
     * @param actionObject Action object that defines the pointer action
     * @param pointerInputState Current state of the input source
     * @param globalKeyInputState Global key input state
     * @throws AppiumException
     */
    public static void dispatchPointerDown(final W3CActionAdapter dispatcherAdapter,
                                           final String sourceId,
                                           final ActionObject actionObject,
                                           final PointerInputState pointerInputState,
                                           final KeyInputState globalKeyInputState) throws AppiumException {
        PointerType pointerType = actionObject.getPointer();
        int button = actionObject.getButton();
        if (pointerInputState.isPressed(button)) {
            return;
        }
        Long x = pointerInputState.getX();
        Long y = pointerInputState.getY();
        // TODO: Do cancel list stuff
        pointerInputState.addPressed(button);
        dispatcherAdapter.lockAdapter();
        dispatcherAdapter.pointerDown(button, sourceId, pointerType, x, y,
                pointerInputState.getButtons(), globalKeyInputState);
        dispatcherAdapter.unlockAdapter();
    }

    /**
     * Perform the 'dispatch pointer up' algorithm
     * @param dispatcherAdapter W3C actions implementation
     * @param sourceId ID of the input source
     * @param actionObject Action object that defines the pointer action
     * @param pointerInputState Current state of the input source
     * @param globalKeyInputState Global key input state
     * @throws AppiumException
     */
    public static void dispatchPointerUp(final W3CActionAdapter dispatcherAdapter,
                                           final String sourceId,
                                           final ActionObject actionObject,
                                           final PointerInputState pointerInputState,
                                           final KeyInputState globalKeyInputState) throws AppiumException {
        PointerType pointerType = actionObject.getPointer();
        int button = actionObject.getButton();
        if (!pointerInputState.isPressed(button)) {
            return;
        }
        Long x = pointerInputState.getX();
        Long y = pointerInputState.getY();
        // TODO: Do cancel list stuff
        pointerInputState.removePressed(button);
        dispatcherAdapter.lockAdapter();
        dispatcherAdapter.pointerUp(button, sourceId, pointerType, x, y,
                pointerInputState.getButtons(), globalKeyInputState);
        dispatcherAdapter.unlockAdapter();
    }

    /**
     * Call the 'dispatch a pointerMove action' algorithm
     * @param dispatcherAdapter W3C actions implementation
     * @param sourceId ID of the input source
     * @param actionObject Action object that defines the pointer action
     * @param pointerInputState Current state of the input source
     * @param tickDuration How long is this tick (in ms)
     * @param timeSinceBeginningOfTick Time since current tick started (in ms)
     * @param globalKeyInputState Global key input state
     * @return
     * @throws AppiumException
     */
    public static Callable<Void> dispatchPointerMove(final W3CActionAdapter dispatcherAdapter,
                                                     final String sourceId,
                                                     final ActionObject actionObject,
                                                     final PointerInputState pointerInputState,
                                                     final long tickDuration,
                                                     final long timeSinceBeginningOfTick,
                                                     final KeyInputState globalKeyInputState) throws AppiumException {
        // 1.5 Variable definitions
        long xOffset = actionObject.getX();
        long yOffset = actionObject.getY();
        long startX = pointerInputState.getX();
        long startY = pointerInputState.getY();
        String origin = actionObject.getOrigin();

        /*Logger.debug(String.format(
            "Dispatching pointer move '%s' on input source with id '%s' with origin '%s' and coordinates [%s, %s]",
            pointerInputState.getType().toString(), sourceId, origin, xOffset, yOffset
        ));*/

        long x;
        long y;

        // 6. Run the substeps of the first matching value of origin
        switch (origin) {
            case VIEWPORT:
                x = xOffset;
                y = yOffset;
                break;
            case POINTER:
                x = startX + xOffset;
                y = startY + yOffset;
                break;
            default:
                long[] elementCoordinates = dispatcherAdapter.getElementCenterPoint(origin);
                x = elementCoordinates[0] + xOffset;
                y = elementCoordinates[1] + yOffset;
                break;
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

        return performPointerMove(
                dispatcherAdapter,
                sourceId,
                pointerInputState,
                duration,
                startX, startY,
                x, y,
                timeSinceBeginningOfTick,
                globalKeyInputState
        );
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
    public static Callable<Void> performPointerMove(final W3CActionAdapter dispatcherAdapter,
                                                    final String sourceId,
                                                    final PointerInputState pointerInputState,
                                                    final long duration,
                                                    final long startX, final long startY,
                                                    final long targetX, final long targetY,
                                                    final long timeSinceBeginningOfTick,
                                                    final KeyInputState globalKeyInputState) {

        /*Logger.debug(String.format(
            "Performing pointer move '%s' on input source with id '%s' from [%s, %s] to [%s, %s]",
            pointerInputState.getType().toString(), sourceId, startX, startY, targetX, targetY
        ));*/
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                boolean isLast;
                do {
                    // 2. Let time delta be the time since the beginning of the current tick, measured in milliseconds on a monotonic clock
                    long timeDelta = System.currentTimeMillis() - timeSinceBeginningOfTick;

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

                    try {
                        dispatcherAdapter.lockAdapter();
                        if (currentX != x || currentY != y) {
                            // 8.2 Perform implementation specific move event
                            dispatcherAdapter.performPointerMoveEvent(sourceId, pointerInputState.getType(), currentX, currentY, x, y,
                                    pointerInputState.getButtons(), globalKeyInputState);

                            // 8.3. Let input state's x property equal x and y property equal y
                            pointerInputState.setX(x);
                            pointerInputState.setY(y);
                        }

                        if (!isLast) {
                            // 10. Asynchronously wait for an implementation defined amount of time to pass
                            dispatcherAdapter.sleep(dispatcherAdapter.pointerMoveIntervalDuration());
                        }
                    } finally {
                        dispatcherAdapter.unlockAdapter();
                    }

                    // 11. Perform a pointer move with arguments source id, input state, duration, start x, start y, target x, target y
                    //     (does this again by going to beginning of loop)
                } while (!isLast);

                // 9. If last is true, return
                return null;
            }
        };
    }
}
