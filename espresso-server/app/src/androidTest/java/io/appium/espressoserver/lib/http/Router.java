package io.appium.espressoserver.lib.http;

import com.google.gson.Gson;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.lib.exceptions.DuplicateRouteException;
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
    private final Map<Method, Map<String, RequestHandler>> routerMap;
    private final Map<Method, Map<String, Class<? extends AppiumParams>>> paramClassMap;

    Router() throws DuplicateRouteException {
        routerMap = new ConcurrentHashMap<>();
        paramClassMap = new ConcurrentHashMap<>();

        addRoute(Method.POST, "/session", new CreateSession(), SessionParams.class);
        addRoute(Method.DELETE, "/session/:sessionId", new DeleteSession(), AppiumParams.class);
        addRoute(Method.GET, "/status", new Status(), AppiumParams.class);
        addRoute(Method.POST, "/session/:sessionId/element", new Finder(), Locator.class);
        addRoute(Method.POST, "/session/:sessionId/element/:elementId/click", new Click(), AppiumParams.class);
        addRoute(Method.POST, "/session/:sessionId/element/:elementId/value", new SendKeys(), TextParams.class);
    }

    /**
     * Registers a route to a handler
     * @param method HTTP method type
     * @param uri URI of endpoint
     * @param handler RequestHandler object that takes params and returns a result based on params
     * @param paramClass Parameters class that the JSON is deserialized to
     * @throws DuplicateRouteException If the same route is registered twice, throw an error
     */
    private void addRoute(Method method, String uri, RequestHandler<? extends AppiumParams, ?> handler, Class<? extends AppiumParams> paramClass) throws DuplicateRouteException {
        if (!routerMap.containsKey(method)) {
            routerMap.put(method, new ConcurrentHashMap<String, RequestHandler>());
        }
        if (routerMap.get(method).containsKey(uri)) {
            throw new DuplicateRouteException();
        }
        if (!paramClassMap.containsKey(method)) {
            paramClassMap.put(method, new ConcurrentHashMap<String, Class<? extends AppiumParams>>());
        }

        routerMap.get(method).put(uri, handler);
        paramClassMap.get(method).put(uri, paramClass);
    }

    BaseResponse route(IHTTPSession session) {
        RequestHandler handler = null;

        String uri = session.getUri();
        Method method = session.getMethod();

        System.out.println("Received " + method + " request for '" + uri + "'");

        if (!routerMap.containsKey(method)) {
            routerMap.put(method, new ConcurrentHashMap<String, RequestHandler>());
        }
        if (!paramClassMap.containsKey(method)) {
            paramClassMap.put(method, new ConcurrentHashMap<String, Class<? extends AppiumParams>>());
        }

        Map<String, String> uriParams = new HashMap<>();

        // Look for a matching route
        // TODO: Move this to a separate method 'isRouteMatch'.
        Class<? extends AppiumParams> paramClass = AppiumParams.class;
        for (Map.Entry<String, RequestHandler> entry : routerMap.get(method).entrySet()) {
            String testUri = entry.getKey();

            // TODO: Use StringBuilder to construct the Test Regexes
            String testRegex = "^";
            Map<Integer, String> wildcardIndices = new HashMap<>();

            // Convert route to a regex to test incoming URI against
            // TODO: Cache these regexes instead of re-creating them every time
            int index = 0;
            for (String uriToken : testUri.split("/")) {
                if (uriToken.startsWith(":")) {
                    testRegex = testRegex.concat("/[\\w\\W]*");
                    wildcardIndices.put(index, uriToken.substring(1));
                } else if (!uriToken.equals("")) {
                    testRegex = testRegex.concat("/" + uriToken);
                }
                index++;
            }
            testRegex += "$";

            // If we have a match, parse the URI params and call that handler
            if (uri.matches(testRegex)) {
                // TODO: Move this to a separate method 'parseUriParams'
                String[] uriTokens = uri.split("/");
                for (Map.Entry<Integer, String> wildcardIndexEntry : wildcardIndices.entrySet()) {
                    int wildcardIndex = wildcardIndexEntry.getKey();
                    uriParams.put(wildcardIndexEntry.getValue(), uriTokens[wildcardIndex]);
                }
                handler = routerMap.get(method).get(entry.getKey());
                paramClass = paramClassMap.get(method).get(entry.getKey());
                break;
            }
        }

        if (handler == null) {
            return new ErrorResponse(NanoHTTPD.Response.Status.NOT_FOUND, String.format("No such route %s", uri));
        }

        // Parse it to an Appium param
        String postJson = parseBody(session);
        AppiumParams appiumParams;
        // TODO: Need to find a way to specify 'required' fields and throw exception when not provided
        if (postJson == null) {
            appiumParams = new AppiumParams();
        } else {
            appiumParams = (AppiumParams) paramClass.cast((new Gson()).fromJson(postJson, paramClass));
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
