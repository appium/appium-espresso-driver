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

package io.appium.espressoserver.lib.http;

import android.util.Log;

import com.google.gson.Gson;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD.Method;
import io.appium.espressoserver.lib.handlers.AcceptAlert;
import io.appium.espressoserver.lib.handlers.Back;
import io.appium.espressoserver.lib.handlers.Clear;
import io.appium.espressoserver.lib.handlers.Click;
import io.appium.espressoserver.lib.handlers.CreateSession;
import io.appium.espressoserver.lib.handlers.DeleteSession;
import io.appium.espressoserver.lib.handlers.DismissAlert;
import io.appium.espressoserver.lib.handlers.ElementEquals;
import io.appium.espressoserver.lib.handlers.ElementScreenshot;
import io.appium.espressoserver.lib.handlers.ElementValue;
import io.appium.espressoserver.lib.handlers.FindActive;
import io.appium.espressoserver.lib.handlers.FindElement;
import io.appium.espressoserver.lib.handlers.FindElements;
import io.appium.espressoserver.lib.handlers.GetAlertText;
import io.appium.espressoserver.lib.handlers.GetAttribute;
import io.appium.espressoserver.lib.handlers.GetDisplayed;
import io.appium.espressoserver.lib.handlers.GetEnabled;
import io.appium.espressoserver.lib.handlers.GetLocation;
import io.appium.espressoserver.lib.handlers.GetLocationInView;
import io.appium.espressoserver.lib.handlers.GetName;
import io.appium.espressoserver.lib.handlers.GetOrientation;
import io.appium.espressoserver.lib.handlers.GetRect;
import io.appium.espressoserver.lib.handlers.GetSelected;
import io.appium.espressoserver.lib.handlers.GetSession;
import io.appium.espressoserver.lib.handlers.GetSessions;
import io.appium.espressoserver.lib.handlers.GetSize;
import io.appium.espressoserver.lib.handlers.GetWindowRect;
import io.appium.espressoserver.lib.handlers.GetWindowSize;
import io.appium.espressoserver.lib.handlers.Keys;
import io.appium.espressoserver.lib.handlers.MultiTouchAction;
import io.appium.espressoserver.lib.handlers.MultiTouchActionsParams;
import io.appium.espressoserver.lib.handlers.NotYetImplemented;
import io.appium.espressoserver.lib.handlers.PerformAction;
import io.appium.espressoserver.lib.handlers.PointerEventHandler;
import io.appium.espressoserver.lib.handlers.PressKeyCode;
import io.appium.espressoserver.lib.handlers.ReleaseActions;
import io.appium.espressoserver.lib.handlers.RequestHandler;
import io.appium.espressoserver.lib.handlers.Screenshot;
import io.appium.espressoserver.lib.handlers.SendKeys;
import io.appium.espressoserver.lib.handlers.SetOrientation;
import io.appium.espressoserver.lib.handlers.Source;
import io.appium.espressoserver.lib.handlers.StartActivity;
import io.appium.espressoserver.lib.handlers.Status;
import io.appium.espressoserver.lib.handlers.Text;
import io.appium.espressoserver.lib.handlers.TouchAction;
import io.appium.espressoserver.lib.handlers.TouchActionsParams;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.handlers.exceptions.MissingCommandsException;
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException;
import io.appium.espressoserver.lib.handlers.exceptions.NoAlertOpenException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.handlers.exceptions.ScreenCaptureException;
import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;
import io.appium.espressoserver.lib.http.response.AppiumResponse;
import io.appium.espressoserver.lib.http.response.BaseResponse;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.AppiumStatus;
import io.appium.espressoserver.lib.model.ElementValueParams;
import io.appium.espressoserver.lib.model.KeyEventParams;
import io.appium.espressoserver.lib.model.Locator;
import io.appium.espressoserver.lib.model.MotionEventParams;
import io.appium.espressoserver.lib.model.OrientationParams;
import io.appium.espressoserver.lib.model.Session;
import io.appium.espressoserver.lib.model.SessionParams;
import io.appium.espressoserver.lib.model.StartActivityParams;
import io.appium.espressoserver.lib.model.TextParams;

import static io.appium.espressoserver.lib.handlers.PointerEventHandler.TouchType.*;
import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;
import static io.appium.espressoserver.lib.helpers.StringHelpers.abbreviate;

class Router {
    private final RouteMap routeMap;

    Router() {
        logger.debug("Generating routes");
        routeMap = new RouteMap();

        routeMap.addRoute(new RouteDefinition(Method.GET, "/status", new Status(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session", new CreateSession(), SessionParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId", new GetSession(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/actions", new PerformAction(), Actions.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId/actions", new ReleaseActions(), Actions.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId", new DeleteSession(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/back", new Back(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/accept_alert", new AcceptAlert(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/dismiss_alert", new DismissAlert(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/alert_text", new GetAlertText(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/orientation", new SetOrientation(), OrientationParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/orientation", new GetOrientation(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/source", new Source(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/screenshot", new Screenshot(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element", new FindElement(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/active", new FindActive(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/active", new FindActive(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/attribute/:name", new GetAttribute(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/clear", new Clear(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/click", new Click(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/displayed", new GetDisplayed(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/element", new FindElement(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/elements", new FindElements(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/enabled", new GetEnabled(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/location", new GetLocation(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/location_in_view", new GetLocationInView(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/name", new GetName(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/rect", new GetRect(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/screenshot", new ElementScreenshot(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/selected", new GetSelected(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/size", new GetSize(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/text", new Text(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/value", new SendKeys(), TextParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/keys", new Keys(), TextParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/window/:windowHandle/size", new GetWindowSize(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/window/rect", new GetWindowRect(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/elements", new FindElements(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/sessions", new GetSessions(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/appium/device/start_activity", new StartActivity(), StartActivityParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/appium/device/press_keycode", new PressKeyCode(false), KeyEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/appium/device/long_press_keycode", new PressKeyCode(true), KeyEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/equals/:otherId", new ElementEquals(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/appium/element/:elementId/value", new ElementValue(false), ElementValueParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/appium/element/:elementId/replace_value", new ElementValue(true), ElementValueParams.class));

        // touch events
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/click", new PointerEventHandler(CLICK), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/longclick", new PointerEventHandler(LONG_CLICK), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/doubleclick", new PointerEventHandler(DOUBLE_CLICK), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/down", new PointerEventHandler(TOUCH_DOWN), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/up", new PointerEventHandler(TOUCH_UP), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/move", new PointerEventHandler(TOUCH_MOVE), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/scroll", new PointerEventHandler(TOUCH_SCROLL), MotionEventParams.class));

        // mouse events
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/buttondown", new PointerEventHandler(MOUSE_DOWN), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/buttonup", new PointerEventHandler(MOUSE_UP), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/moveto", new PointerEventHandler(MOUSE_MOVE), MotionEventParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/click", new PointerEventHandler(MOUSE_CLICK), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/doubleclick", new PointerEventHandler(MOUSE_DOUBLECLICK), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/perform", new TouchAction(), TouchActionsParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/multi/perform", new MultiTouchAction(), MultiTouchActionsParams.class));

        // Not implemented
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/flick", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/alert_text", new NotYetImplemented(), AppiumParams.class));

        // Probably will never implement
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/context", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/contexts", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/contexts", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/timeouts", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/timeouts/async_script", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/timeouts/implicit_wait", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/window_handle", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/window_handles", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/url", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/url", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/forward", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/refresh", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/execute", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/execute_async", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/ime/available_engines", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/ime/active_engine", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/ime/activated", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/ime/deactivate", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/ime/activate", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/frame", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/window", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/window/:windowhandle/maximize", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/cookie", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/cookie", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId/cookie", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId/cookie/:name", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/title", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/submit", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/css/:propertyName", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/location", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/location", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/log", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/log/types", new NotYetImplemented(), AppiumParams.class));
    }

    @SuppressWarnings("unchecked")
    public BaseResponse route(String uri, Method method, Map<String, String> params,  Map<String, String> files) {
        logger.debug(String.format("Started processing %s request for '%s'", method, uri));

        // Look for a route that matches this URL
        RouteDefinition matchingRoute = routeMap.findMatchingRoute(method, uri);

        // If no route found, return a 404 Error Response
        if (matchingRoute == null) {
            return new AppiumResponse<>(AppiumStatus.UNKNOWN_ERROR, String.format("No such route %s", uri));
        }
        logger.debug(String.format("Matched route definition: %s", matchingRoute.getClass()));

        // Get the handler, parameter class and URI parameters
        RequestHandler handler = matchingRoute.getHandler();
        logger.debug(String.format("Matched route handler: %s", handler.getClass()));
        Class<? extends AppiumParams> paramClass = matchingRoute.getParamClass();
        Map<String, String> uriParams = matchingRoute.getUriParams(uri);

        // Get the appium params
        String postJson = files.get("postData");

        AppiumParams appiumParams;
        if (postJson == null) {
            appiumParams = new AppiumParams();
        } else {
            logger.debug(String.format("Got raw post data: %s", abbreviate(postJson, 300)));
            appiumParams = paramClass.cast((new Gson()).fromJson(postJson, paramClass));
        }
        appiumParams.initUriMapping(uriParams);

        // Validate the sessionId
        if (appiumParams.getSessionId() != null && !appiumParams.getSessionId().equals(Session.getGlobalSession().getId())) {
            return new AppiumResponse<>(AppiumStatus.UNKNOWN_ERROR, "Invalid session ID " + appiumParams.getSessionId());
        }

        // Create the result
        try {
            Object handlerResult = handler.handle(appiumParams);
            String sessionId = appiumParams.getSessionId();

            // If it's a new session, pull out the newly created Session ID
            if (handlerResult != null && handlerResult.getClass() == Session.class) {
                sessionId = ((Session) handlerResult).getId();
            }

            AppiumResponse appiumResponse = new AppiumResponse<>(AppiumStatus.SUCCESS, handlerResult, sessionId);
            logger.debug(String.format("Finished processing %s request for '%s'", method, uri));
            return appiumResponse;
        } catch (NoSuchElementException e) {
            return new AppiumResponse<>(AppiumStatus.NO_SUCH_ELEMENT, Log.getStackTraceString(e));
        } catch (SessionNotCreatedException e) {
            return new AppiumResponse<>(AppiumStatus.SESSION_NOT_CREATED_EXCEPTION, Log.getStackTraceString(e));
        } catch (InvalidStrategyException e) {
            return new AppiumResponse<>(AppiumStatus.INVALID_SELECTOR, Log.getStackTraceString(e));
        } catch (NotYetImplementedException | MissingCommandsException e) {
            return new AppiumResponse<>(AppiumStatus.UNKNOWN_COMMAND, Log.getStackTraceString(e));
        } catch (StaleElementException e) {
            return new AppiumResponse<>(AppiumStatus.STALE_ELEMENT_REFERENCE, Log.getStackTraceString(e));
        } catch (XPathLookupException e) {
            return new AppiumResponse<>(AppiumStatus.XPATH_LOOKUP_ERROR, Log.getStackTraceString(e));
        } catch (NoAlertOpenException e) {
            return new AppiumResponse<>(AppiumStatus.NO_ALERT_OPEN_ERROR, Log.getStackTraceString(e));
        } catch (ScreenCaptureException e) {
            return new AppiumResponse<>(AppiumStatus.UNABLE_TO_CAPTURE_SCREEN_ERROR, Log.getStackTraceString(e));
        } catch (InvalidElementStateException e) {
            return new AppiumResponse<>(AppiumStatus.INVALID_ELEMENT_STATE, Log.getStackTraceString(e));
        } catch (InvalidArgumentException e) {
            return new AppiumResponse<>(AppiumStatus.INVALID_ARGUMENT, Log.getStackTraceString(e));
        } catch (MoveTargetOutOfBoundsException e) {
            return new AppiumResponse<>(AppiumStatus.MOVE_TARGET_OUT_OF_BOUNDS, Log.getStackTraceString(e));
        } catch (Exception e) {
            return new AppiumResponse<>(AppiumStatus.UNKNOWN_ERROR, Log.getStackTraceString(e));
        }
    }
}
