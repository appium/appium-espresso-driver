package io.appium.espressoserver.lib.http;

import com.google.gson.Gson;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.lib.handlers.Click;
import io.appium.espressoserver.lib.handlers.CreateSession;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException;
import io.appium.espressoserver.lib.handlers.Finder;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.MissingCommandsException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.RequestHandler;
import io.appium.espressoserver.lib.handlers.DeleteSession;
import io.appium.espressoserver.lib.handlers.SendKeys;
import io.appium.espressoserver.lib.handlers.Status;
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

        // TODO: Map EVERY JSONWP route and have it throw not yet implemented if there's no handler
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session", new CreateSession(), SessionParams.class));
        routeMap.addRoute(new RouteDefinition(Method.DELETE, "/session/:sessionId", new DeleteSession(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.GET, "/status", new Status(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element", new Finder(), Locator.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/click", new Click(), AppiumParams.class));
        routeMap.addRoute(new RouteDefinition(Method.POST, "/session/:sessionId/element/:elementId/value", new SendKeys(), TextParams.class));
    }

    @SuppressWarnings("WeakerAccess")
    public BaseResponse route(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        // Look for a route that matches this URL
        RouteDefinition matchingRoute = routeMap.findMatchingRoute(uri);

        // If no route found, return a 404 Error Response
        if (matchingRoute == null) {
            return new ErrorResponse(NanoHTTPD.Response.Status.NOT_FOUND, String.format("No such route %s", uri));
        }

        // Get the handler, parameter class and URI parameters
        RequestHandler handler = matchingRoute.getHandler();
        Class<? extends AppiumParams> paramClass = matchingRoute.getParamClass();
        Map<String, String> uriParams = matchingRoute.getUriParams(uri);

        // Parse the appium params
        String postJson = parseBody(session);
        AppiumParams appiumParams;
        if (postJson == null) {
            appiumParams = new AppiumParams();
        } else {
            appiumParams = paramClass.cast((new Gson()).fromJson(postJson, paramClass));
        }
        appiumParams.setSessionId(uriParams.get("sessionId"));
        appiumParams.setElementId(uriParams.get("elementId"));

        // Validate the sessionId
        if (appiumParams.getSessionId() != null && !appiumParams.getSessionId().equals(Session.getGlobalSessionId())) {
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
            return new AppiumResponse<>(AppiumStatus.NO_SUCH_ELEMENT, e.getMessage());
        } catch (SessionNotCreatedException e) {
            return new AppiumResponse<>(AppiumStatus.SESSION_NOT_CREATED_EXCEPTION, e.getMessage());
        } catch(InvalidStrategyException e) {
            return new AppiumResponse<>(AppiumStatus.INVALID_SELECTOR, e.getMessage());
        } catch (MissingCommandsException e) {
            return new ErrorResponse(NanoHTTPD.Response.Status.NOT_FOUND, e.getMessage());
        } catch (AppiumException e) {
            return new AppiumResponse<>(AppiumStatus.UNKNOWN_ERROR, e.getMessage());
        }
    }

    private String parseBody (IHTTPSession session) {
        String result = "{}";
        try {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);
            result = files.get("postData");
        } catch (IOException e) {
            // TODO: error handling
        } catch (NanoHTTPD.ResponseException e) {
            // TODO: handle error
        }
        return result;
    }
}
