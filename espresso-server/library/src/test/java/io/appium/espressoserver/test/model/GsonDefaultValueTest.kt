package io.appium.espressoserver.test.model

import com.google.gson.Gson
import io.appium.espressoserver.lib.model.ClipboardDataType
import io.appium.espressoserver.lib.model.DURATION_MILLIS
import io.appium.espressoserver.lib.model.GetClipboardParams
import io.appium.espressoserver.lib.model.KeyEventParams
import io.appium.espressoserver.lib.model.MOUSE_LEFT
import io.appium.espressoserver.lib.model.MobileBackdoorMethod
import io.appium.espressoserver.lib.model.MobileBackdoorParams
import io.appium.espressoserver.lib.model.MotionEventParams
import io.appium.espressoserver.lib.model.MoveToParams
import io.appium.espressoserver.lib.model.MultiTouchActionsParams
import io.appium.espressoserver.lib.model.REPEAT_COUNT
import io.appium.espressoserver.lib.model.ScrollToPageParams
import io.appium.espressoserver.lib.model.SetClipboardParams
import io.appium.espressoserver.lib.model.SettingsParams
import io.appium.espressoserver.lib.model.ToastLookupParams
import io.appium.espressoserver.lib.model.ViewFlashParams
import io.appium.espressoserver.lib.model.web.WebAtomsParams
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GsonDefaultValueTest {
    private val gson = Gson()

    @Test
    fun `should use default values for GetClipboardParams and SetClipboardParams`() {
        val getClipboard = gson.fromJson("{}", GetClipboardParams::class.java)
        assertEquals(ClipboardDataType.PLAINTEXT, getClipboard.contentType)

        val setClipboard = gson.fromJson("""{"content": "abc", "contentType": null}""", SetClipboardParams::class.java)
        assertEquals(ClipboardDataType.PLAINTEXT, setClipboard.contentType)
        assertEquals("abc", setClipboard.content)
    }

    @Test
    fun `should use default values for ViewFlashParams when omitted or null`() {
        val emptyParams = gson.fromJson("{}", ViewFlashParams::class.java)
        assertEquals(DURATION_MILLIS, emptyParams.durationMillis)
        assertEquals(REPEAT_COUNT, emptyParams.repeatCount)

        val nullParams = gson.fromJson("""{"durationMillis": null, "repeatCount": null}""", ViewFlashParams::class.java)
        assertEquals(DURATION_MILLIS, nullParams.durationMillis)
        assertEquals(REPEAT_COUNT, nullParams.repeatCount)
    }

    @Test
    fun `should use default values for ToastLookupParams when omitted or null`() {
        val emptyParams = gson.fromJson("{}", ToastLookupParams::class.java)
        assertEquals("", emptyParams.text)
        assertEquals(false, emptyParams.isRegexp)

        val nullParams = gson.fromJson("""{"text": null, "isRegexp": null}""", ToastLookupParams::class.java)
        assertEquals("", nullParams.text)
        assertEquals(false, nullParams.isRegexp)
    }

    @Test
    fun `should use default values for ScrollToPageParams when omitted or null`() {
        val emptyParams = gson.fromJson("{}", ScrollToPageParams::class.java)
        assertEquals(false, emptyParams.smoothScroll)

        val nullParams = gson.fromJson("""{"smoothScroll": null}""", ScrollToPageParams::class.java)
        assertEquals(false, nullParams.smoothScroll)
    }

    @Test
    fun `should use default values for MotionEventParams when omitted or null`() {
        val emptyParams = gson.fromJson("{}", MotionEventParams::class.java)
        assertEquals(0L, emptyParams.x)
        assertEquals(0L, emptyParams.y)
        assertEquals(MOUSE_LEFT, emptyParams.button)

        val nullParams = gson.fromJson("""{"x": null, "y": null, "button": null}""", MotionEventParams::class.java)
        assertEquals(0L, nullParams.x)
        assertEquals(0L, nullParams.y)
        assertEquals(MOUSE_LEFT, nullParams.button)
    }

    @Test
    fun `should use default values for MoveToParams and KeyEventParams`() {
        val moveTo = gson.fromJson("""{"xOffset": null, "yOffset": null}""", MoveToParams::class.java)
        assertEquals(0, moveTo.xOffset)
        assertEquals(0, moveTo.yOffset)

        val keyEvent = gson.fromJson("""{"keycode": null, "metastate": null, "flags": null}""", KeyEventParams::class.java)
        assertEquals(0, keyEvent.keycode)
        assertEquals(0, keyEvent.metastate)
        assertEquals(0, keyEvent.flags)
    }

    @Test
    fun `should use default collections when omitted or null`() {
        val backdoorMethod = gson.fromJson("""{"name": "test", "args": null}""", MobileBackdoorMethod::class.java)
        assertTrue(backdoorMethod.args.isEmpty())

        val backdoorParams = gson.fromJson("""{"target": "activity", "methods": null}""", MobileBackdoorParams::class.java)
        assertTrue(backdoorParams.methods.isEmpty())

        val multiTouchParams = gson.fromJson("""{"actions": null}""", MultiTouchActionsParams::class.java)
        assertTrue(multiTouchParams.actions.isEmpty())

        val settingsParams = gson.fromJson("""{"settings": null}""", SettingsParams::class.java)
        assertTrue(settingsParams.settings.isEmpty())

        val webAtomsParams = gson.fromJson("""{"webviewEl": "abc", "forceJavascriptEnabled": true}""", WebAtomsParams::class.java)
        assertEquals("abc", webAtomsParams.webviewEl)
        assertEquals(true, webAtomsParams.forceJavascriptEnabled)
        assertTrue(webAtomsParams.methodChain.isEmpty())
    }
}
