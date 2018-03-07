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

import com.google.gson.Gson;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import io.appium.espressoserver.lib.handlers.AcceptAlert;
import io.appium.espressoserver.lib.handlers.ElementScreenshot;
import io.appium.espressoserver.lib.handlers.FindActive;
import io.appium.espressoserver.lib.handlers.GetAlertText;
import io.appium.espressoserver.lib.handlers.Clear;
import io.appium.espressoserver.lib.handlers.DismissAlert;
import io.appium.espressoserver.lib.handlers.GetAttribute;
import io.appium.espressoserver.lib.handlers.Back;
import io.appium.espressoserver.lib.handlers.Click;
import io.appium.espressoserver.lib.handlers.CreateSession;
import io.appium.espressoserver.lib.handlers.GetDisplayed;
import io.appium.espressoserver.lib.handlers.GetEnabled;
import io.appium.espressoserver.lib.handlers.FindElements;
import io.appium.espressoserver.lib.handlers.GetLocation;
import io.appium.espressoserver.lib.handlers.GetLocationInView;
import io.appium.espressoserver.lib.handlers.GetRect;
import io.appium.espressoserver.lib.handlers.GetOrientation;
import io.appium.espressoserver.lib.handlers.MoveTo;
import io.appium.espressoserver.lib.handlers.GetName;
import io.appium.espressoserver.lib.handlers.NotYetImplemented;
import io.appium.espressoserver.lib.handlers.GetSession;
import io.appium.espressoserver.lib.handlers.GetSessions;
import io.appium.espressoserver.lib.handlers.Screenshot;
import io.appium.espressoserver.lib.handlers.GetSelected;
import io.appium.espressoserver.lib.handlers.GetSize;
import io.appium.espressoserver.lib.handlers.SetOrientation;
import io.appium.espressoserver.lib.handlers.Source;
import io.appium.espressoserver.lib.handlers.Text;
import io.appium.espressoserver.lib.handlers.W3CActions;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.handlers.exceptions.NoAlertOpenException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.handlers.exceptions.ScreenCaptureException;
import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException;
import io.appium.espressoserver.lib.handlers.Finder;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.MissingCommandsException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.RequestHandler;
import io.appium.espressoserver.lib.handlers.DeleteSession;
import io.appium.espressoserver.lib.handlers.SendKeys;
import io.appium.espressoserver.lib.handlers.Status;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.handlers.exceptions.XPathLookupException;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.http.response.AppiumResponse;
import io.appium.espressoserver.lib.http.response.BaseResponse;
import io.appium.espressoserver.lib.http.response.ErrorResponse;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.AppiumStatus;
import io.appium.espressoserver.lib.model.Locator;
import io.appium.espressoserver.lib.model.MoveToParams;
import io.appium.espressoserver.lib.model.OrientationParams;
import io.appium.espressoserver.lib.model.Session;
import io.appium.espressoserver.lib.model.SessionParams;
import io.appium.espressoserver.lib.model.TextParams;
import io.appium.espressoserver.lib.model.W3CActionsParams;

class Router {
    private final RouteMap routeMap;

    Router() {
        Logger.debug("Generating routes");
        routeMap = new RouteMap();

        routeMap.addRoute(new RouteDefinition(Method.GET, "/status", new Status(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session", new CreateSession(), SessionParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId", new GetSession(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/actions", new W3CActions(), W3CActionsParams.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId", new DeleteSession(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/back", new Back(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/accept_alert", new AcceptAlert(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/dismiss_alert", new DismissAlert(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/alert_text", new GetAlertText(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/orientation", new SetOrientation(), OrientationParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/orientation", new GetOrientation(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/source", new Source(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/screenshot", new Screenshot(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element", new Finder(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/active", new FindActive(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/active", new FindActive(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/attribute/:name", new GetAttribute(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/clear", new Clear(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/click", new Click(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/displayed", new GetDisplayed(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/element", new Finder(), Locator.class));
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
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/elements", new FindElements(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/moveto", new MoveTo(), MoveToParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/sessions", new GetSessions(), AppiumParams.class));

        // Mobile JSON Wire Protocol endpoints
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/context", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/contexts", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/contexts", new NotYetImplemented(), AppiumParams.class));

        // Unimplemented
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
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/window/:windowhandle/size", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/window/:windowhandle/maximize", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/cookie", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/cookie", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId/cookie", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId/cookie/:name", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/title", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/submit", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/keys", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/equals/:otherId", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/css/:propertyName", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/alert_text", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/attribute", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/click", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/click", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/down", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/up", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/move", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/longclick", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/touch/flick", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/location", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/location", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/log", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/log/types", new NotYetImplemented(), AppiumParams.class));
    }


    @SuppressWarnings("unchecked")
    public BaseResponse route(String uri, Method method, Map<String, String> params,  Map<String, String> files) {
        // Look for a route that matches this URL
        RouteDefinition matchingRoute = routeMap.findMatchingRoute(method, uri);

        // If no route found, return a 404 Error Response
        if (matchingRoute == null) {
            return new ErrorResponse(NanoHTTPD.Response.Status.NOT_FOUND, String.format("No such route %s", uri));
        }

        // Get the handler, parameter class and URI parameters
        RequestHandler handler = matchingRoute.getHandler();
        Class<? extends AppiumParams> paramClass = matchingRoute.getParamClass();
        Map<String, String> uriParams = matchingRoute.getUriParams(uri);

        // Get the appium params
        String postJson = files.get("postData");

        AppiumParams appiumParams;
        if (postJson == null) {
            appiumParams = new AppiumParams();
        } else {
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
            Logger.debug(String.format("Finished processing %s request for '%s'", method, uri));
            return appiumResponse;
        } catch (NoSuchElementException e) {
            return new AppiumResponse<>(e, AppiumStatus.NO_SUCH_ELEMENT, e.getMessage());
        } catch (SessionNotCreatedException e) {
            return new AppiumResponse<>(e, AppiumStatus.SESSION_NOT_CREATED_EXCEPTION, e.getMessage());
        } catch (InvalidStrategyException e) {
            return new AppiumResponse<>(e, AppiumStatus.INVALID_SELECTOR, e.getMessage());
        } catch (MissingCommandsException e) {
            return new ErrorResponse(e, NanoHTTPD.Response.Status.NOT_FOUND, e.getMessage());
        } catch (NotYetImplementedException e) {
            return new ErrorResponse(e, NanoHTTPD.Response.Status.NOT_IMPLEMENTED, e.getMessage());
        } catch (StaleElementException e) {
            return new AppiumResponse<>(e, AppiumStatus.STALE_ELEMENT_REFERENCE, e.getMessage());
        } catch (XPathLookupException e) {
            return new AppiumResponse<>(e, AppiumStatus.XPATH_LOOKUP_ERROR, e.getMessage());
        } catch (NoAlertOpenException e) {
            return new AppiumResponse<>(e, AppiumStatus.NO_ALERT_OPEN_ERROR, e.getMessage());
        } catch (ScreenCaptureException e) {
            return new AppiumResponse<>(e, AppiumStatus.UNABLE_TO_CAPTURE_SCREEN_ERROR, e.getMessage());
        } catch (InvalidElementStateException e) {
            return new AppiumResponse<>(e, AppiumStatus.INVALID_ELEMENT_STATE, e.getMessage());
        } catch (AppiumException e) {
            e.printStackTrace();
            return new AppiumResponse<>(e, AppiumStatus.UNKNOWN_ERROR, e.getMessage());
        }
    }
}
