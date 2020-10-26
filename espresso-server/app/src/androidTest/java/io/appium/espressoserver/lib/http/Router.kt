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

package io.appium.espressoserver.lib.http

import com.google.gson.Gson
import com.google.gson.JsonParseException
import fi.iki.elonen.NanoHTTPD.Method
import io.appium.espressoserver.lib.handlers.*
import io.appium.espressoserver.lib.handlers.PointerEventHandler.TouchType.*
import io.appium.espressoserver.lib.handlers.TouchAction
import io.appium.espressoserver.lib.handlers.exceptions.*
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.StringHelpers.abbreviate
import io.appium.espressoserver.lib.helpers.w3c.models.Actions
import io.appium.espressoserver.lib.http.response.AppiumResponse
import io.appium.espressoserver.lib.http.response.BaseResponse
import io.appium.espressoserver.lib.model.*
import io.appium.espressoserver.lib.model.web.WebAtomsParams

internal class Router {
    private val routeMap: RouteMap

    init {
        AndroidLogger.debug("Generating routes")
        routeMap = RouteMap()

        routeMap.addRoute(RouteDefinition(Method.GET, "/status", Status(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session", CreateSession(), SessionParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId", GetSession(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/actions", PerformAction(), Actions::class.java))
        routeMap.addRoute(RouteDefinition(Method.DELETE, "/session/:sessionId/actions", ReleaseActions(), Actions::class.java))
        routeMap.addRoute(RouteDefinition(Method.DELETE, "/session/:sessionId", DeleteSession(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/back", Back(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/accept_alert", AcceptAlert(), AlertParams::class.java))
        // alias
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/alert/accept", AcceptAlert(), AlertParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/dismiss_alert", DismissAlert(), AlertParams::class.java))
        // alias
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/alert/dismiss", DismissAlert(), AlertParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/alert_text", GetAlertText(), AppiumParams::class.java))
        // alias
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/alert/text", GetAlertText(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/orientation", SetOrientation(), OrientationParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/orientation", GetOrientation(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/rotation", SetRotation(), RotationParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/rotation", GetRotation(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/source", Source(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/screenshot", ScreenshotHandler(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/element", FindElement(), Locator::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/active", FindActive(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/element/active", FindActive(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/attribute/:name", GetAttribute(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/clear", Clear(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/click", Click(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/displayed", GetDisplayed(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/element", FindElement(), Locator::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/elements", FindElements(), Locator::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/enabled", GetEnabled(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/location", GetLocation(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/location_in_view", GetLocationInView(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/name", GetName(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/rect", GetRect(), AppiumParams::class.java))
        // W3C endpoint
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/screenshot", ElementScreenshot(), AppiumParams::class.java))
        // JSONWP endpoint
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/screenshot/:elementId", ElementScreenshot(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/selected", GetSelected(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/size", GetSize(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/text", Text(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/value", ElementValue(false), TextValueParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/keys", Keys(), TextValueParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/window/:windowHandle/size", GetWindowSize(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/window/rect", GetWindowRect(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/elements", FindElements(), Locator::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/sessions", GetSessions(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/appium/device/info", GetDeviceInfo(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/device/hide_keyboard", HideKeyboard(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/device/start_activity", StartActivity(), StartActivityParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/device/press_keycode", PressKeyCode(false), KeyEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/device/long_press_keycode", PressKeyCode(true), KeyEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/device/perform_editor_action", PerformEditorAction(), EditorActionParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/equals/:otherId", ElementEquals(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/element/:elementId/value", ElementValue(false), TextValueParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/element/:elementId/replace_value", ElementValue(true), TextValueParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/device/get_clipboard", GetClipboard(), GetClipboardParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/device/set_clipboard", SetClipboard(), SetClipboardParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/device/open_notifications", OpenNotifications(), AppiumParams::class.java))

        // touch events
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/click", PointerEventHandler(CLICK), MotionEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/longclick", PointerEventHandler(LONG_CLICK), MotionEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/doubleclick", PointerEventHandler(DOUBLE_CLICK), MotionEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/down", PointerEventHandler(TOUCH_DOWN), MotionEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/up", PointerEventHandler(TOUCH_UP), MotionEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/move", PointerEventHandler(TOUCH_MOVE), MotionEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/scroll", PointerEventHandler(TOUCH_SCROLL), MotionEventParams::class.java))

        // mouse events
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/buttondown", PointerEventHandler(MOUSE_DOWN), MotionEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/buttonup", PointerEventHandler(MOUSE_UP), MotionEventParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/moveto", MoveTo(), MoveToParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/click", PointerEventHandler(MOUSE_CLICK), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/doubleclick", PointerEventHandler(MOUSE_DOUBLECLICK), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/perform", TouchAction(), TouchActionsParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/multi/perform", MultiTouchAction(), MultiTouchActionsParams::class.java))

        // 'execute mobile' commands
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/swipe", MobileSwipe(), MobileSwipeParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/is_toast_displayed", GetToastVisibility(), ToastLookupParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/open_drawer", DrawerActionHandler(true), DrawerActionParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/close_drawer", DrawerActionHandler(false), DrawerActionParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/set_date", SetDate(), SetDateParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/set_time", SetTime(), SetTimeParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/scroll_to_page", ScrollToPage(), ScrollToPageParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/navigate_to", NavigateTo(), NavigateToParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/backdoor", MobileBackdoor(), MobileBackdoorParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/flash", MobileViewFlash(), ViewFlashParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/uiautomator", Uiautomator(), UiautomatorParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/appium/execute_mobile/uiautomator_page_source", UiautomatorPageSource(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/click_action", MobileClickAction(), MobileClickActionParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/web_atoms", WebAtoms(), WebAtomsParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/:elementId/dismiss_autofill", PerformAutofillDismissal(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/start_service", StartService(), StartServiceParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/stop_service", StopService(), StopServiceParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/register_idling_resources", RegisterIdlingResources(), IdlingResourcesParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/appium/execute_mobile/unregister_idling_resources", UnregisterIdlingResources(), IdlingResourcesParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/appium/execute_mobile/list_idling_resources", ListIdlingResources(), AppiumParams::class.java))

        // Not implemented
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/touch/flick", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/alert_text", NotYetImplemented(), AppiumParams::class.java))

        // Probably will never implement
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/context", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/contexts", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/contexts", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/timeouts", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/timeouts/async_script", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/timeouts/implicit_wait", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/window_handle", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/window_handles", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/url", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/url", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/forward", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/refresh", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/execute", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/execute_async", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/ime/available_engines", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/ime/active_engine", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/ime/activated", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/ime/deactivate", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/ime/activate", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/frame", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/window", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/window/:windowhandle/maximize", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/cookie", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/cookie", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.DELETE, "/session/:sessionId/cookie", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.DELETE, "/session/:sessionId/cookie/:name", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/title", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/submit", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/css/:propertyName", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/location", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/location", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.POST, "/session/:sessionId/log", NotYetImplemented(), AppiumParams::class.java))
        routeMap.addRoute(RouteDefinition(Method.GET, "/session/:sessionId/log/types", NotYetImplemented(), AppiumParams::class.java))
    }

    fun route(uri: String, method: Method, files: Map<String, String>): BaseResponse {
        AndroidLogger.debug("Started processing $method request for '$uri'")

        // Look for a route that matches this URL
        val matchingRoute = routeMap.findMatchingRoute(method, uri)
                ?: return AppiumResponse(AppiumException("No such route: $uri"))

        // If no route found, return a 404 Error Response
        AndroidLogger.debug("Matched route definition: ${matchingRoute.javaClass}")

        // Get the handler, parameter class and URI parameters
        val handler = matchingRoute.handler
        AndroidLogger.debug("Matched route handler: ${handler.javaClass}")
        val paramClass = matchingRoute.paramClass
        val uriParams = matchingRoute.getUriParams(uri)

        // Get the appium params
        val postJson = files["postData"]

        var sessionId: String? = null
        try {
            // Parse the parameters
            val appiumParams: AppiumParams = postJson?.let {
                AndroidLogger.debug("Got raw post data: ${abbreviate(it, 300)}")
                try {
                    paramClass.cast(Gson().fromJson<AppiumParams>(it, paramClass))
                            ?: return AppiumResponse(InvalidArgumentException("Could not parse JSON: $it"))
                } catch (e: JsonParseException) {
                    // If failed to parse params, throw an invalid argument exception
                    return AppiumResponse(InvalidArgumentException(e))
                }
            } ?: AppiumParams()
            appiumParams.initUriMapping(uriParams)

            // Validate the sessionId
            if (appiumParams.sessionId != null
                    && GlobalSession.exists && appiumParams.sessionId != GlobalSession.sessionId) {
                return AppiumResponse(NoSuchDriverException("Invalid session ID ${appiumParams.sessionId!!}"))
            }

            // Execute the matching handler
            sessionId = appiumParams.sessionId
            val handlerResult = handler.handle(appiumParams)

            // If it's a new session, pull out the newly created Session ID
            if (handlerResult is GlobalSession) {
                sessionId = handlerResult.sessionId
            }

            // Construct the response and serve it
            val appiumResponse = AppiumResponse(handlerResult, sessionId)
            AndroidLogger.debug("Finished processing $method request for '$uri'")
            return appiumResponse
        } catch (e: Throwable) {
            var err = e
            if (e is NoClassDefFoundError) {
                err = IllegalStateException("${e.message} " +
                        "Consider adding the appropriate dependency containing the missing class into " +
                        "'additionalAndroidTestDependencies' and/or 'additionalAppDependencies' property values of " +
                        "'espressoBuildConfig' capability", e)
            }
            return AppiumResponse(err, sessionId)
        }
    }
}
