package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso

import android.os.SystemClock
import android.text.Editable
import android.text.Selection
import android.view.View
import android.widget.TextView
import androidx.test.espresso.UiController
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import io.appium.espressoserver.lib.compose.ComposeHandlerBridge
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.helpers.AndroidLogger

private const val FOCUS_TIMEOUT_MS = 1000L
private const val FOCUS_POLL_MS = 50L

/**
 * Types characters that have no key code (e.g. Cyrillic, CJK, emoji) by editing
 * the focused text view directly. Must be used on the main thread.
 */
class NonAsciiTextInserter(private val uiController: UiController) {

    fun insert(text: String) {
        val codePoints = text.codePoints().toArray()
        if (codePoints.isEmpty()) {
            throw InvalidArgumentException("Cannot type an empty key value")
        }
        codePoints.firstOrNull { !isTypeableCodePoint(it) }?.let {
            throw InvalidArgumentException(
                    "The code point $it is neither a known W3C special key nor a printable character that could be typed")
        }
        uiController.loopMainThreadUntilIdle()
        val focused = waitForFocusedEditor()
        if (focused is TextView) {
            val editable = focused.text as? Editable
                    ?: throw InvalidElementStateException("The focused element does not expose editable text")
            val inserted = insertIntoEditable(editable, text)
            AndroidLogger.info("Typed $inserted of ${text.length} chars into the focused element")
        } else if (ComposeHandlerBridge.insertTextIntoFocusedNode(focused, text)) {
            AndroidLogger.info("Typed ${text.length} chars into the focused Compose element")
        } else {
            throw InvalidElementStateException("The focused Compose element rejected the text")
        }
        uiController.loopMainThreadUntilIdle()
    }

    private fun waitForFocusedEditor(): View {
        val deadline = SystemClock.uptimeMillis() + FOCUS_TIMEOUT_MS
        var focused: View?
        while (true) {
            focused = findFocusedView()
            if (focused is TextView && focused.onCheckIsTextEditor()
                    || focused != null && ComposeHandlerBridge.hasFocusedEditableNode(focused)) {
                return focused
            }
            if (SystemClock.uptimeMillis() >= deadline) {
                break
            }
            uiController.loopMainThreadForAtLeast(FOCUS_POLL_MS)
        }
        throw InvalidElementStateException(
                if (focused == null) "Cannot type the character: no element is focused"
                else "Cannot type the character: the focused element is not editable")
    }

    // Dialogs and popups have their own windows, so look at all app roots, the focused window first
    private fun findFocusedView(): View? {
        val roots = getRootViews()
        val ordered = roots.filter { it.hasWindowFocus() } + roots.filterNot { it.hasWindowFocus() }.asReversed()
        return ordered.firstNotNullOfOrNull { it.findFocus() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getRootViews(): List<View> {
        try {
            val wmgClass = Class.forName("android.view.WindowManagerGlobal")
            val wmg = wmgClass.getMethod("getInstance").invoke(null)
            val views = wmgClass.getDeclaredField("mViews").apply { isAccessible = true }.get(wmg)
            return (views as List<View>).filter { it.isShown }
        } catch (e: ReflectiveOperationException) {
            AndroidLogger.info("Cannot enumerate root views, using resumed activities only: ${e.message}")
        }
        return ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .mapNotNull { it.window?.decorView?.rootView }
    }

    companion object {
        fun isTypeableCodePoint(codePoint: Int): Boolean {
            if (!Character.isValidCodePoint(codePoint) || Character.isISOControl(codePoint)) {
                return false
            }
            val type = Character.getType(codePoint)
            return type != Character.PRIVATE_USE.toInt()
                    && type != Character.UNASSIGNED.toInt()
                    && type != Character.SURROGATE.toInt()
        }

        /**
         * Replaces the selection (or appends if there is none) and puts the cursor after the new text.
         * Input filters apply, so fewer chars (or none) may be inserted.
         * @return the number of chars actually inserted
         */
        fun insertIntoEditable(editable: Editable, text: String): Int {
            val selStart = Selection.getSelectionStart(editable)
            val selEnd = Selection.getSelectionEnd(editable)
            val (start, end) = if (selStart < 0 || selEnd < 0) {
                editable.length to editable.length
            } else {
                minOf(selStart, selEnd) to maxOf(selStart, selEnd)
            }
            val lengthBefore = editable.length
            editable.replace(start, end, text)
            val inserted = editable.length - (lengthBefore - (end - start))
            Selection.setSelection(editable, (start + inserted).coerceIn(0, editable.length))
            return inserted
        }
    }
}
