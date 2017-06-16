package io.appium.espressoserver.lib.http;

import com.google.gson.Gson;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import io.appium.espressoserver.lib.handlers.Back;
import io.appium.espressoserver.lib.handlers.Click;
import io.appium.espressoserver.lib.handlers.CreateSession;
import io.appium.espressoserver.lib.handlers.FindElements;
import io.appium.espressoserver.lib.handlers.NotYetImplemented;
import io.appium.espressoserver.lib.handlers.GetSession;
import io.appium.espressoserver.lib.handlers.GetSessions;
import io.appium.espressoserver.lib.handlers.Screenshot;
import io.appium.espressoserver.lib.handlers.Source;
import io.appium.espressoserver.lib.handlers.Text;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
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
import io.appium.espressoserver.lib.http.response.AppiumResponse;
import io.appium.espressoserver.lib.http.response.BaseResponse;
import io.appium.espressoserver.lib.http.response.ErrorResponse;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.AppiumStatus;
import io.appium.espressoserver.lib.model.Locator;
import io.appium.espressoserver.lib.model.Session;
import io.appium.espressoserver.lib.model.SessionParams;
import io.appium.espressoserver.lib.model.TextParams;

class Router {
    private final RouteMap routeMap;


    Router() {
        System.out.println("Generating routes");
        routeMap = new RouteMap();

        routeMap.addRoute(new RouteDefinition(Method.GET, "/status", new Status(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/sessions", new GetSessions(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId", new GetSession(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/source", new Source(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/screenshot", new Screenshot(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/text", new Text(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session", new CreateSession(), SessionParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element", new Finder(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/click", new Click(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/value", new SendKeys(), TextParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/back", new Back(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/elements", new FindElements(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId", new DeleteSession(), AppiumParams.class));

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
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/active", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/element", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/elements", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/submit", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/keys", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/name", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/clear", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/selected", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/enabled", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/attribute/:name", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/equals/:otherId", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/displayed", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/location", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/location_in_view", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/size", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/css/:propertyName", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/orientation", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/orientation", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/alert_text", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/alert_text", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/session/:sessionId/element/:elementId/attribute", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/accept_alert", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/dismiss_alert", new NotYetImplemented(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/moveto", new NotYetImplemented(), AppiumParams.class));
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
        appiumParams.setSessionId(uriParams.get("sessionId"));
        appiumParams.setElementId(uriParams.get("elementId"));

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
            System.out.println("Finished processing " + method + " request for '" + uri + "'");
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
        } catch (AppiumException e) {
            e.printStackTrace();
            return new AppiumResponse<>(e, AppiumStatus.UNKNOWN_ERROR, e.getMessage());
        }
    }
}
