package io.appium.espressoserver.lib.helpers.w3c.dispatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.*;

public class PointerDispatch {

    /*public static Future<Void> dispatchPointerMove(final W3CActionAdapter dispatcherAdapter,
                                                   final String sourceId,
                                                   final ActionObject actionObject,
                                                   final PointerInputState pointerInputState,
                                                   final int tickDuration) {
        long xOffset = actionObject.getX();
        long yOffset = actionObject.getY();
        int startX = pointerInputState.getX();
        int startY = pointerInputState.getY();
        String origin = actionObject.getOrigin();

        long x;
        long y;

        // Do null-check to keep linter happy. Can't actually be null.
        if (origin != null) {
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
                    // TODO: Get element center point
                    break;
            }
        }
    }*/

    /**
     * Implements the 'perform a pointer move' algorithm in section 17.4.3
     * @param dispatcherAdapter
     * @param sourceId
     * @param pointerInputState
     * @param duration
     * @param startX
     * @param startY
     * @param targetX
     * @param targetY
     */
    public static Future<Void> performPointerMove(final W3CActionAdapter dispatcherAdapter,
                                                  final String sourceId,
                                                  final PointerInputState pointerInputState,
                                                  final int duration,
                                                  final int startX, final int startY,
                                                  final int targetX, final int targetY,
                                                  final long timeSinceBeginningOfTick,
                                                  final KeyInputState globalKeyInputState) {

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<Void> callable;

        callable = new Callable<Void>() {
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
                    final int x = isLast ? targetX : Math.round(durationRatio * (targetX - startX)) + startX;
                    final int y = isLast ? targetY : Math.round(durationRatio * (targetY - startY)) + startY;

                    // 7-8: Let currentX and currentY be pointer input state
                    final int currentX = pointerInputState.getX();
                    final int currentY = pointerInputState.getY();

                    if (currentX != x || currentY != y) {
                        // 8.2 Perform implementation specific move event
                        dispatcherAdapter.lockAdapter();
                        dispatcherAdapter.pointerMoveEvent(sourceId, pointerInputState.getType(), currentX, currentY, x, y,
                                pointerInputState.getButtons(), globalKeyInputState);
                        dispatcherAdapter.unlockAdapter();

                        // 8.3. Let input state's x property equal x and y property equal y
                        pointerInputState.setX(x);
                        pointerInputState.setY(y);
                    }

                    if (!isLast) {
                        // 10. Asynchronously wait for an implementation defined amount of time to pass
                        dispatcherAdapter.lockAdapter();
                        dispatcherAdapter.sleep(dispatcherAdapter.pointerMoveIntervalDuration());
                        dispatcherAdapter.unlockAdapter();
                    }

                    // 11. Perform a pointer move with arguments source id, input state, duration, start x, start y, target x, target y
                    //     (does this again by going to beginning of loop)
                } while (!isLast);

                // 9. If last is true, return
                executorService.shutdown();
                return null;
            }
        };

        return executorService.submit(callable);
    }
}
