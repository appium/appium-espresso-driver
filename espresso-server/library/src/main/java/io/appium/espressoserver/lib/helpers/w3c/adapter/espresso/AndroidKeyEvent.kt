package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso

import android.os.SystemClock
import android.view.KeyCharacterMap
import android.view.KeyEvent
import androidx.test.espresso.InjectEventSecurityException
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ALT
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ARROW_DOWN
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ARROW_LEFT
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ARROW_RIGHT
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ARROW_UP
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ASTERISK
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.BACKSPACE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.CLEAR
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.COMMA
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.CONTROL
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.DELETE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.EIGHT
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.END
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.EQUALS
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ESCAPE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F1
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F10
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F11
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F12
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F2
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F3
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F4
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F5
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F6
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F7
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F8
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F9
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.FIVE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.FORWARD_SLASH
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.FOUR
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.HELP
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.HOME
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.HYPHEN
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.INSERT
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.META
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.NINE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ONE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PAGEDOWN
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PAGEUP
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PAUSE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PERIOD
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PLUS
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.RETURN
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.SEMICOLON
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.SEVEN
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.SHIFT
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.SIX
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.TAB
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.THREE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.TWO
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.UNIDENTIFIED
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.WHITESPACE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ZENKAKU_HANKAKU
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ZERO
import java.util.*


/**
 * Translate a W3C Key into an Android Key Event (if possible
 * @param keyValue The W3C normalized key value (https://www.w3.org/TR/webdriver1/#keyboard-actions)
 * @param location The W3C "key location"
 * @return
 */
fun keyCodeToEvent(keyValue: String?, @Suppress("UNUSED_PARAMETER") location: Int): Int {
    return when (keyValue) {
        UNIDENTIFIED -> KeyEvent.KEYCODE_UNKNOWN
        HELP -> KeyEvent.KEYCODE_HELP
        BACKSPACE -> KeyEvent.KEYCODE_DEL
        TAB -> KeyEvent.KEYCODE_TAB
        CLEAR -> KeyEvent.KEYCODE_CLEAR
        RETURN -> KeyEvent.KEYCODE_ENTER
        PAUSE -> KeyEvent.KEYCODE_BREAK
        ESCAPE -> KeyEvent.KEYCODE_ESCAPE
        WHITESPACE -> KeyEvent.KEYCODE_SPACE
        SEMICOLON -> KeyEvent.KEYCODE_SEMICOLON
        EQUALS -> KeyEvent.KEYCODE_EQUALS
        ZERO -> KeyEvent.KEYCODE_0
        ONE -> KeyEvent.KEYCODE_1
        TWO -> KeyEvent.KEYCODE_2
        THREE -> KeyEvent.KEYCODE_3
        FOUR -> KeyEvent.KEYCODE_4
        FIVE -> KeyEvent.KEYCODE_5
        SIX -> KeyEvent.KEYCODE_6
        SEVEN -> KeyEvent.KEYCODE_7
        EIGHT -> KeyEvent.KEYCODE_8
        NINE -> KeyEvent.KEYCODE_9
        ASTERISK -> KeyEvent.KEYCODE_STAR
        PLUS -> KeyEvent.KEYCODE_NUMPAD_ADD
        COMMA -> KeyEvent.KEYCODE_COMMA
        HYPHEN -> KeyEvent.KEYCODE_MINUS
        PERIOD -> KeyEvent.KEYCODE_PERIOD
        FORWARD_SLASH -> KeyEvent.KEYCODE_SLASH
        F1 -> KeyEvent.KEYCODE_F1
        F2 -> KeyEvent.KEYCODE_F2
        F3 -> KeyEvent.KEYCODE_F3
        F4 -> KeyEvent.KEYCODE_F4
        F5 -> KeyEvent.KEYCODE_F5
        F6 -> KeyEvent.KEYCODE_F6
        F7 -> KeyEvent.KEYCODE_F7
        F8 -> KeyEvent.KEYCODE_F8
        F9 -> KeyEvent.KEYCODE_F9
        F10 -> KeyEvent.KEYCODE_F10
        F11 -> KeyEvent.KEYCODE_F11
        F12 -> KeyEvent.KEYCODE_F12
        ZENKAKU_HANKAKU -> KeyEvent.KEYCODE_ZENKAKU_HANKAKU
        SHIFT -> KeyEvent.KEYCODE_SHIFT_LEFT
        CONTROL -> KeyEvent.KEYCODE_CTRL_LEFT
        ALT -> KeyEvent.KEYCODE_ALT_LEFT
        META -> KeyEvent.KEYCODE_META_LEFT
        PAGEUP -> KeyEvent.KEYCODE_PAGE_UP
        PAGEDOWN -> KeyEvent.KEYCODE_PAGE_DOWN
        END -> KeyEvent.KEYCODE_MOVE_END
        HOME -> KeyEvent.KEYCODE_HOME
        ARROW_LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
        ARROW_UP -> KeyEvent.KEYCODE_DPAD_UP
        ARROW_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
        ARROW_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        INSERT -> KeyEvent.KEYCODE_INSERT
        DELETE -> KeyEvent.KEYCODE_FORWARD_DEL
        else -> -1
    }
}

private const val IME_SETTLE_MS = 150L

// Shared across instances: the legacy /keys handler creates a new AndroidKeyEvent per request
private object ImeSettle {
    private var lastWasInsertion = false
    private var lastInputAt = 0L

    /** Waits until the IME had time to process the previous input if it was of the other kind. */
    fun await(uiController: UiController, isInsertion: Boolean) {
        val remaining = IME_SETTLE_MS - (SystemClock.uptimeMillis() - lastInputAt)
        if (lastInputAt > 0 && lastWasInsertion != isInsertion && remaining > 0) {
            uiController.loopMainThreadForAtLeast(remaining)
        }
    }

    fun mark(isInsertion: Boolean) {
        lastWasInsertion = isInsertion
        lastInputAt = SystemClock.uptimeMillis()
    }
}

class AndroidKeyEvent @JvmOverloads constructor(
        private val uiController: UiController,
        // Overridable for tests; returns null for chars without key events
        private val charsToEvents: (CharArray) -> Array<KeyEvent>? =
                KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)::getEvents
) {
    private val keyDownTimes: MutableMap<String?, List<KeyEvent>> = HashMap()
    private val insertedKeys: MutableSet<String?> = HashSet()
    private val textInserter by lazy { NonAsciiTextInserter(uiController) }

    @Throws(AppiumException::class)
    fun keyDown(w3cKeyEvent: W3CKeyEvent) {
        keyUpOrDown(w3cKeyEvent, true)
    }

    @Throws(AppiumException::class)
    fun keyUp(w3cKeyEvent: W3CKeyEvent) {
        keyUpOrDown(w3cKeyEvent, false)
    }

    /**
     * Translate a string into a sequence of Android Key Events
     * @param key A key in String form
     * @param isDown Returns true if this is a key down events
     * @param isRepeat Returns true if this is a repeat key down
     * @param metaState The current global meta state of Android key events
     * @return The events or null if the key has no Android key event representation
     */
    private fun convertStringToAndroidKeyEvents(key: String?, isDown: Boolean,
                                                isRepeat: Boolean, metaState: Int): List<KeyEvent>? {
        val keyEventsFromChar = charsToEvents(key!!.toCharArray()) ?: return null
        val now = SystemClock.uptimeMillis()
        val keyEvents: MutableList<KeyEvent> = ArrayList()
        var keyEventIndex = 0
        while (keyEventIndex * 2 < keyEventsFromChar.size) {
            // getEvents produces UP and DOWN events, so we need to skip over every other event
            val keyEvent = keyEventsFromChar[keyEventIndex * 2]
            val downTime = if (isDown) SystemClock.uptimeMillis() else keyDownTimes[key]!![keyEventIndex].downTime
            keyEvents.add(KeyEvent(
                    downTime,
                    if (isDown) downTime else now,
                    if (isDown) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP,
                    keyEvent.keyCode,
                    if (isRepeat) 1 else 0,
                    metaState or keyEvent.metaState,
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0
            ))
            keyEventIndex++
        }
        return keyEvents
    }

    /**
     * Dispatch an Android key event
     * @param w3cKeyEvent Key Event in W3C form (https://www.w3.org/TR/webdriver1/#keyboard-actions)
     * @param isDown Returns true if it's keyDown, otherwise it's keyUp
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    private fun keyUpOrDown(w3cKeyEvent: W3CKeyEvent, isDown: Boolean) {
        val action = if (isDown) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP
        val key = w3cKeyEvent.key
        val keyCode = w3cKeyEvent.keyCode

        // Characters without key codes are typed on keyDown, so their keyUp is a no-op
        if (!isDown && insertedKeys.remove(key)) {
            return
        }
        // No-op if releasing a key that isn't down
        if (action == KeyEvent.ACTION_UP && !keyDownTimes.containsKey(key)) {
            return
        }
        val keyEvents: List<KeyEvent>
        keyEvents = if (keyCode >= 0) {

            // If the keyCode is known, send it now
            val downTime = if (isDown) SystemClock.uptimeMillis() else keyDownTimes[key]!![0].downTime
            listOf(KeyEvent(
                    downTime,
                    if (isDown) downTime else SystemClock.uptimeMillis(),
                    action,
                    keyCode,
                    if (w3cKeyEvent.isRepeat) 1 else 0,
                    getMetaState(w3cKeyEvent),
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0
            ))
        } else {
            // If there's no keyCode, map the characters to Android keys
            convertStringToAndroidKeyEvents(key, isDown, w3cKeyEvent.isRepeat, getMetaState(w3cKeyEvent))
                    ?: run {
                        insertText(w3cKeyEvent)
                        return
                    }
        }
        injectKeyEvents(isDown, keyEvents, key)
    }

    @Throws(AppiumException::class)
    private fun insertText(w3cKeyEvent: W3CKeyEvent) {
        if (w3cKeyEvent.isAltKey || w3cKeyEvent.isCtrlKey || w3cKeyEvent.isShiftKey || w3cKeyEvent.isMetaKey) {
            throw InvalidArgumentException(
                    "Cannot type a character that has no key code while modifier keys are pressed")
        }
        // The IME handles key events and direct edits asynchronously, so switching needs a pause
        ImeSettle.await(uiController, true)
        textInserter.insert(w3cKeyEvent.key ?: "")
        ImeSettle.mark(true)
        insertedKeys.add(w3cKeyEvent.key)
    }

    /**
     * Get the Android key meta state from a W3C key event
     * @param keyEvent W3C key event
     * @return
     */
    private fun getMetaState(keyEvent: W3CKeyEvent): Int {
        var metaState = 0
        if (keyEvent.isAltKey) {
            metaState = metaState or KeyEvent.META_ALT_MASK
        }
        if (keyEvent.isCtrlKey) {
            metaState = metaState or KeyEvent.META_CTRL_MASK
        }
        if (keyEvent.isShiftKey) {
            metaState = metaState or KeyEvent.META_SHIFT_MASK
        }
        return metaState
    }

    /**
     * Inject a key event into UiController
     * @param isDown Is it a key down event?
     * @param keyEvents A list of the Android Key Events to inject
     * @param key The key to inject in string form (for logging purposes)
     * @throws AppiumException
     */
    @Throws(AppiumException::class)
    private fun injectKeyEvents(isDown: Boolean, keyEvents: List<KeyEvent>, key: String?) {
        // If it's a keydown event, record that this key went down. If it's keyup, remove pre-existing
        // record of this key going down
        if (isDown) {
            keyDownTimes[key] = keyEvents
        } else {
            keyDownTimes.remove(key)
        }

        ImeSettle.await(uiController, false)

        // Inject all of the key events, in order
        for (androidKeyEvent in keyEvents) {
            AndroidLogger.info(String.format("Calling key event: %s", androidKeyEvent))
            var isSuccess: Boolean
            isSuccess = try {
                uiController.injectKeyEvent(androidKeyEvent)
            } catch (e: InjectEventSecurityException) {
                throw AppiumException(e.cause.toString())
            }
            if (!isSuccess) {
                throw AppiumException(String.format("Could not inject key event %s", key))
            }
        }
        ImeSettle.mark(false)
    }
}