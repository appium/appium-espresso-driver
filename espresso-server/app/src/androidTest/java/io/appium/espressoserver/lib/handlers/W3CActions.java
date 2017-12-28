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

package io.appium.espressoserver.lib.handlers;

import android.os.SystemClock;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewInteraction;
import android.util.LongSparseArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.helpers.w3c_actions.ActionsHelpers;
import io.appium.espressoserver.lib.helpers.w3c_actions.ActionsHelpers.InputEventParams;
import io.appium.espressoserver.lib.helpers.w3c_actions.ActionsHelpers.KeyInputEventParams;
import io.appium.espressoserver.lib.helpers.w3c_actions.ActionsHelpers.MotionInputEventParams;
import io.appium.espressoserver.lib.helpers.w3c_actions.ActionsParseException;
import io.appium.espressoserver.lib.model.W3CActionsParams;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static io.appium.espressoserver.lib.helpers.w3c_actions.ActionsHelpers.actionsToInputEventsMapping;
import static io.appium.espressoserver.lib.helpers.w3c_actions.ActionsHelpers.getPointerAction;
import static io.appium.espressoserver.lib.helpers.w3c_actions.ActionsHelpers.metaKeysToState;
import static io.appium.espressoserver.lib.helpers.w3c_actions.ActionsHelpers.toolTypeToInputSource;

public class W3CActions implements RequestHandler<W3CActionsParams, Void> {

    private static final List<Integer> HOVERING_ACTIONS = Arrays.asList(
            MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_EXIT, MotionEvent.ACTION_HOVER_MOVE
    );

    /**
     * Android handler for <a href="https://github.com/jlipps/simple-wd-spec#perform-actions">W3C actions endpoint</a>
     * <p>
     * All input source types are supported as well as multi-touch gestures.
     * <p>
     * The following additional item options are supported for <b>pointer</b> source:
     * <ul>
     * <li>pressure - A value in range [0.0, 1.0], which defines pointer pressure,
     * where 1.0 is the normal pressure (the default value) and 0.0 means no pressure.</li>
     * <li>size - A normalized value that describes the approximate size of the pointer touch area
     * in relation to the maximum detectable size of the device.
     * It represents some approximation of the area of the screen being
     * pressed; the actual value in pixels corresponding to the
     * touch is normalized with the device specific range of values
     * and scaled to a value between 0 and 1 (the default value).
     * The value of size can be used to determine fat touch events.</li>
     * </ul>
     * <p>
     * Applicable key and meta key codes for <b>key</b> input source can be found in
     * {@link KeyEvent} documentation. Value transformation to a numeric key code is
     * done via {@link String#charAt(int)} method call, which means, for example,
     * that the value <em>"\\u2000"</em> equals to meta key code 0x2000
     * {@link KeyEvent#META_CTRL_LEFT_ON}. Meta key codes are also applied to pointer
     * events, which are happening at the same moment.
     * <p>
     *
     * @param params JSON request formatted according to W3C actions endpoint compilation rules.
     */
    @Override
    public Void handle(W3CActionsParams params) throws AppiumException {
        final String actionsAsJson = new Gson().toJson(params.getActions());
        try {
            final JSONArray actions = ActionsHelpers.preprocessActions(new JSONArray(actionsAsJson));
            if (executeActions(actions)) {
                return null;
            }
        } catch (JSONException | ActionsParseException e) {
            Logger.error("Exception while reading JSON: ", e);
            throw new AppiumException(String.format("Could not parse W3C actions %s",
                    actionsAsJson), e);
        } catch (Exception e) {
            Logger.error("Exception while executing W3C actions: ", e);
            throw new AppiumException(String.format(
                    "Got an unexpected error while executing W3C actions %s", actionsAsJson), e);
        }
        throw new AppiumException(String.format("Cannot execute W3C actions %s", actionsAsJson));
    }

    private UiController getUiController() {
        final ViewInteraction root = onView(isRoot());
        try {
            final Field f = root.getClass().getDeclaredField("uiController");
            f.setAccessible(true);
            return (UiController) f.get(root);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean injectEvent(UiController uiController, InputEvent event)
            throws InjectEventSecurityException {
        if (event instanceof KeyEvent) {
            return uiController.injectKeyEvent((KeyEvent) event);
        }
        return uiController.injectMotionEvent((MotionEvent) event);
    }

    private static PointerProperties[] filterPointerProperties(
            final List<MotionInputEventParams> motionEventsParams, final boolean shouldHovering) {
        final List<PointerProperties> result = new ArrayList<>();
        for (final MotionInputEventParams eventParams : motionEventsParams) {
            if (shouldHovering && HOVERING_ACTIONS.contains(eventParams.actionCode)) {
                result.add(eventParams.properties);
            } else if (!shouldHovering && !HOVERING_ACTIONS.contains(eventParams.actionCode)) {
                result.add(eventParams.properties);
            }
        }
        return result.toArray(new PointerProperties[result.size()]);
    }

    private static PointerCoords[] filterPointerCoordinates(
            final List<MotionInputEventParams> motionEventsParams, final boolean shouldHovering) {
        final List<PointerCoords> result = new ArrayList<>();
        for (final MotionInputEventParams eventParams : motionEventsParams) {
            if (shouldHovering && HOVERING_ACTIONS.contains(eventParams.actionCode)) {
                result.add(eventParams.coordinates);
            } else if (!shouldHovering && !HOVERING_ACTIONS.contains(eventParams.actionCode)) {
                result.add(eventParams.coordinates);
            }
        }
        return result.toArray(new PointerCoords[result.size()]);
    }

    private boolean executeActions(final JSONArray actions) throws JSONException, InjectEventSecurityException {
        final LongSparseArray<List<InputEventParams>> inputEventsMapping = actionsToInputEventsMapping(actions);
        final List<Long> allDeltas = new ArrayList<>();
        for (int i = 0; i < inputEventsMapping.size(); i++) {
            allDeltas.add(inputEventsMapping.keyAt(i));
        }
        Collections.sort(allDeltas);

        long recentTimeDelta = 0;
        boolean result = true;
        final Set<Integer> depressedMetaKeys = new HashSet<>();
        final long startTimestamp = SystemClock.uptimeMillis();
        final UiController uiController = getUiController();
        final LongSparseArray<Integer> motionEventsBalanceByInputSource = new LongSparseArray<>();
        for (final Long currentTimeDelta : allDeltas) {
            final List<InputEventParams> eventParams = inputEventsMapping.get(currentTimeDelta);
            final LongSparseArray<List<MotionInputEventParams>> motionParamsByInputSource = new LongSparseArray<>();
            for (final InputEventParams eventParam : eventParams) {
                if (eventParam instanceof KeyInputEventParams) {
                    final int keyCode = ((KeyInputEventParams) eventParam).keyCode;
                    final int keyAction = ((KeyInputEventParams) eventParam).keyAction;
                    if (keyCode > KeyEvent.getMaxKeyCode()) {
                        if (keyAction == KeyEvent.ACTION_DOWN) {
                            depressedMetaKeys.add(keyCode);
                        } else {
                            depressedMetaKeys.remove(keyCode);
                        }
                    } else {
                        result &= injectEvent(uiController, new KeyEvent(startTimestamp + eventParam.startDelta,
                                SystemClock.uptimeMillis(), keyAction, keyCode, metaKeysToState(depressedMetaKeys),
                                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
                    }
                } else if (eventParam instanceof MotionInputEventParams) {
                    final int inputSource = toolTypeToInputSource(((MotionInputEventParams) eventParam).properties.toolType);
                    final List<MotionInputEventParams> events = (motionParamsByInputSource.get(inputSource) == null) ?
                            new ArrayList<MotionInputEventParams>() :
                            motionParamsByInputSource.get(inputSource);
                    events.add((MotionInputEventParams) eventParam);
                    motionParamsByInputSource.put(inputSource, events);
                }
            }

            for (int i = 0; i < motionParamsByInputSource.size(); i++) {
                final int inputSource = (int) motionParamsByInputSource.keyAt(i);
                final List<MotionInputEventParams> motionEventsParams = motionParamsByInputSource.valueAt(i);
                final PointerProperties[] nonHoveringProps = filterPointerProperties(motionEventsParams,
                        false);
                final PointerProperties[] hoveringProps = filterPointerProperties(motionEventsParams,
                        true);
                final PointerCoords[] nonHoveringCoords = filterPointerCoordinates(motionEventsParams,
                        false);
                final PointerCoords[] hoveringCoords = filterPointerCoordinates(motionEventsParams,
                        true);

                for (final MotionInputEventParams motionEventParams : motionEventsParams) {
                    final int actionCode = motionEventParams.actionCode;
                    Integer upDownBalance = motionEventsBalanceByInputSource.get(inputSource);
                    if (upDownBalance == null) {
                        upDownBalance = 0;
                    }
                    switch (actionCode) {
                        case MotionEvent.ACTION_DOWN: {
                            ++upDownBalance;
                            motionEventsBalanceByInputSource.put(inputSource, upDownBalance);
                            final int action = upDownBalance == 1 ? MotionEvent.ACTION_DOWN :
                                    getPointerAction(MotionEvent.ACTION_POINTER_DOWN, upDownBalance - 1);
                            result &= injectEvent(uiController, MotionEvent.obtain(startTimestamp + motionEventParams.startDelta,
                                    SystemClock.uptimeMillis(), action,
                                    action == MotionEvent.ACTION_DOWN ? 1 : upDownBalance, nonHoveringProps, nonHoveringCoords,
                                    metaKeysToState(depressedMetaKeys), motionEventParams.button,
                                    1, 1, 0, 0, inputSource, 0));
                        }
                        break;
                        case MotionEvent.ACTION_UP: {
                            if (upDownBalance <= 0) {
                                // ignore unbalanced pointer up actions
                                break;
                            }
                            motionEventsBalanceByInputSource.put(inputSource, upDownBalance);
                            final int action = upDownBalance <= 1 ? MotionEvent.ACTION_UP :
                                    getPointerAction(MotionEvent.ACTION_POINTER_UP, upDownBalance - 1);
                            result &= injectEvent(uiController, MotionEvent.obtain(startTimestamp + motionEventParams.startDelta,
                                    SystemClock.uptimeMillis(), action, action == MotionEvent.ACTION_UP ? 1 : upDownBalance,
                                    nonHoveringProps, nonHoveringCoords, metaKeysToState(depressedMetaKeys), motionEventParams.button,
                                    1, 1, 0, 0, inputSource, 0));
                            if (upDownBalance > 0) {
                                --upDownBalance;
                            }
                        }
                        break;
                        case MotionEvent.ACTION_MOVE: {
                            result &= injectEvent(uiController, MotionEvent.obtain(startTimestamp + motionEventParams.startDelta,
                                    SystemClock.uptimeMillis(), actionCode, upDownBalance,
                                    nonHoveringProps, nonHoveringCoords, metaKeysToState(depressedMetaKeys),
                                    motionEventParams.button, 1, 1, 0, 0, inputSource, 0)
                            );
                        }
                        break;
                        case MotionEvent.ACTION_HOVER_ENTER:
                        case MotionEvent.ACTION_HOVER_EXIT:
                        case MotionEvent.ACTION_HOVER_MOVE: {
                            result &= injectEvent(uiController, MotionEvent.obtain(startTimestamp + motionEventParams.startDelta,
                                    SystemClock.uptimeMillis(), actionCode, hoveringProps.length,
                                    hoveringProps, hoveringCoords, metaKeysToState(depressedMetaKeys),
                                    0, 1, 1, 0, 0, inputSource, 0)
                            );
                        }
                        break;
                        default:
                            // do nothing
                            break;
                    } // switch
                } // motionEventParams : motionEventsParams
            } // for i < motionParamsByInputSource.size()
//            uiController.loopMainThreadForAtLeast(currentTimeDelta - recentTimeDelta);
            SystemClock.sleep(currentTimeDelta - recentTimeDelta);
            recentTimeDelta = currentTimeDelta;
        } // currentTimeDelta : allDeltas
        return result;
    }
}
