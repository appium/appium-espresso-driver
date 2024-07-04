package io.appium.espressoserver.lib.helpers.w3c.dispatcher

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyNormalizer
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.getCode
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.getLocation
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState

/**
 * Implement key dispatch events (https://www.w3.org/TR/webdriver/#keyboard-actions)
 */
@Throws(AppiumException::class)
private fun dispatchKeyEvent(dispatcherAdapter: W3CActionAdapter,
                             actionObject: ActionObject,
                             inputState: KeyInputState,
                             inputStateTable: InputStateTable,
                             @Suppress("UNUSED_PARAMETER") tickDuration: Float,
                             down: Boolean): W3CKeyEvent {
    // Get the base Key Event
    val keyEvent = getKeyEvent(dispatcherAdapter, actionObject.value!!)
    val key = keyEvent.key!!

    // 3. If the input state's pressed property contains key, let repeat be true, otherwise let repeat be false.
    if (inputState.isPressed(key)) {
        keyEvent.isRepeat = true
    }

    // 6. Let charCode, keyCode and which be the implementation-specific values of the charCode,
    // keyCode and which properties.
    // Omiting this for now until there's an implementation that needs it.

    // 7-10: Set the alt, shift, ctrl meta key state
    dispatcherAdapter.logger.info(String.format(
            "Dispatching '%s' event on input source with id %s with properties '%s'",
            if (down) "keyDown" else "keyUp", actionObject.id, keyEvent.logMessage()
    ))
    if (down) {
        inputState.isAlt = inputState.isAlt || key == NormalizedKeys.ALT
        inputState.isShift = inputState.isShift || key == NormalizedKeys.SHIFT
        inputState.isCtrl = inputState.isCtrl || key == NormalizedKeys.CONTROL
        inputState.isMeta = inputState.isMeta || key == NormalizedKeys.META
    } else {
        inputState.isAlt = inputState.isAlt && key != NormalizedKeys.ALT
        inputState.isShift = inputState.isShift && key != NormalizedKeys.SHIFT
        inputState.isCtrl = inputState.isCtrl && key != NormalizedKeys.CONTROL
        inputState.isMeta = inputState.isMeta && key != NormalizedKeys.META
    }

    // Set the alt, shift, ctrl and meta states on the Key Event
    keyEvent.isAltKey = inputState.isAlt
    keyEvent.isCtrlKey = inputState.isCtrl
    keyEvent.isShiftKey = inputState.isShift
    keyEvent.isMetaKey = inputState.isMeta

    // 11: Add key to the set corresponding to input state's pressed property
    if (down) {
        inputState.addPressed(key)
    } else {
        inputState.removePressed(key)
    }

    // Must lock the dispatcherAdapter in-case other threads are also using it
    dispatcherAdapter.lockAdapter()
    try {
        if (down) {

            // 12: Append action object with subtype property changed to 'keyUp' to cancel list
            val cancelActionObject = ActionObject(actionObject)
            cancelActionObject.subType = InputSource.ActionType.KEY_UP
            inputStateTable.addActionToCancel(cancelActionObject)

            // 13: Call implementation specific key-down event
            dispatcherAdapter.keyDown(keyEvent)
        } else {
            // 13: Call implementation specific key-up event
            dispatcherAdapter.keyUp(keyEvent)
        }
    } finally {
        dispatcherAdapter.unlockAdapter()
    }

    // Log the updated keyboard state
    dispatcherAdapter.logger.info(String.format(
            "Current state of key input source with id: %s",
            inputState.logMessage()
    ))
    return keyEvent
}

/**
 * Dispatch a key down event (in accordance with https://www.w3.org/TR/webdriver/#keyboard-actions)
 *
 * @param dispatcherAdapter Adapter that has implementation specific keyDown event
 * @param actionObject W3C Action Object
 * @param inputState State of the input source
 * @param inputStateTable All of the input states for this session
 * @param tickDuration How long the 'tick' is. This is unused currently but may be used in the future
 * @return W3CKeyEvent (for testing purposes). 'null' if the dispatch failed.
 */
@Throws(AppiumException::class)
fun dispatchKeyDown(dispatcherAdapter: W3CActionAdapter,
                    actionObject: ActionObject, inputState: KeyInputState,
                    inputStateTable: InputStateTable, tickDuration: Float = 0f): W3CKeyEvent? =
     dispatchKeyEvent(dispatcherAdapter, actionObject, inputState, inputStateTable, tickDuration, true)

/**
 * Dispatch a key up event (in accordance with https://www.w3.org/TR/webdriver/#keyboard-actions)
 *
 * @param dispatcherAdapter Adapter that has implementation specific keyDown event
 * @param actionObject W3C Action Object
 * @param inputState State of the input source
 * @param inputStateTable All of the input states for this session
 * @param tickDuration How long the 'tick' is. This is unused currently but may be used in the future
 * @return W3CKeyEvent (for testing purposes). 'null' if the dispatch failed.
 */
@Throws(AppiumException::class)
fun dispatchKeyUp(dispatcherAdapter: W3CActionAdapter,
                  actionObject: ActionObject, inputState: KeyInputState,
                  inputStateTable: InputStateTable, tickDuration: Float = 0f): W3CKeyEvent? =
        dispatchKeyEvent(dispatcherAdapter, actionObject, inputState, inputStateTable, tickDuration, false)

/**
 * Helper method to get a Key Event that has common attributes between KeyUp and KeyDispatch
 * @param rawKey The value of the key stroke
 * @return W3CKeyEvent Key event info used by adapter
 */
@Throws(AppiumException::class)
fun getKeyEvent(dispatcherAdapter: W3CActionAdapter, rawKey: String): W3CKeyEvent = W3CKeyEvent().apply {
    // 1. Let raw key be action's value property

    // 2. Let key be equal to the normalised key value for raw key
    this.key = KeyNormalizer.toNormalizedKey(rawKey)

    // 4. Let code be 'code' for raw keypress
    this.code = getCode(rawKey)

    // 5. Let location be the key location for raw key
    this.location = getLocation(rawKey)

    // 6. Let charCode, keyCode and which be the implementation-specific values of the charCode,
    // keyCode and which properties appropriate for a key with key key and location location on
    // a 102 key US keyboard
    this.keyCode = dispatcherAdapter.getKeyCode(this.key, this.location)
    this.charCode = dispatcherAdapter.getCharCode(this.code, this.location)
    this.which = dispatcherAdapter.getWhich(this.key, this.location)
}
