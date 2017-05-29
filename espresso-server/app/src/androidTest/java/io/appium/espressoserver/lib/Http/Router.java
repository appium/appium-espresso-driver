package io.appium.espressoserver.lib.Http;

import com.google.gson.Gson;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.ResponseException;
import io.appium.espressoserver.lib.Exceptions.DuplicateRouteException;
//import io.appium.espressoserver.lib.Handlers.Click;
//import io.appium.espressoserver.lib.Handlers.Finder;
import io.appium.espressoserver.lib.Handlers.RequestHandler;
//import io.appium.espressoserver.lib.Handlers.DeleteSession;
//import io.appium.espressoserver.lib.Handlers.SendKeys;
//import io.appium.espressoserver.lib.Handlers.Status;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.ErrorResponse;
import io.appium.espressoserver.lib.Model.AppiumParams;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Session;
import io.appium.espressoserver.lib.Model.SessionParams;

class Router {
    private final Map<Method, HashMap<String, RequestHandler>> routerMap;
    private final Map<Method, HashMap<String, Class>> paramClassMap;

    Router() throws DuplicateRouteException {
        routerMap = new HashMap<>();
        paramClassMap = new HashMap<>();

        addRoute(Method.POST, "/session", new io.appium.espressoserver.lib.Handlers.CreateSession(), SessionParams.class);
        //addRoute(Method.DELETE, "/session/:sessionId", new DeleteSession());
        //addRoute(Method.GET, "/status", new Status());
        //addRoute(Method.POST, "/session/:sessionId/element", new Finder());
        //addRoute(Method.POST, "/session/:sessionId/element/:elementId/click", new Click());
        //addRoute(Method.POST, "/session/:sessionId/element/:elementId/value", new SendKeys());
    }

    private void addRoute(Method method, String uri, RequestHandler handler, Class<? extends AppiumParams> paramClass) throws DuplicateRouteException {
        if (!routerMap.containsKey(method)) {
            routerMap.put(method, new HashMap<String, RequestHandler>());
        }
        if (routerMap.get(method).containsKey(uri)) {
            throw new DuplicateRouteException();
        }
        if (!paramClassMap.containsKey(method)) {
            paramClassMap.put(method, new HashMap<String, Class>());
        }

        routerMap.get(method).put(uri, handler);
        paramClassMap.get(method).put(uri, paramClass);
    }

    AppiumResponse route(IHTTPSession session) {
        RequestHandler handler = null;

        String uri = session.getUri();
        Method method = session.getMethod();

        System.out.println("Received " + method + " request for '" + uri + "'");

        if (!routerMap.containsKey(method)) {
            routerMap.put(method, new HashMap<String, RequestHandler>());
        }

        Map<String, String> uriParams = new HashMap<>();

        // Look for a matching route
        // TODO: Move this to a separate method 'isRouteMatch'.
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
                break;
            }
        }

        if (handler != null) {
            // Parse it to an Appium param (TODO: Should have classes that extend AppiumParams)
            Gson gson = new Gson();
            String postJson = parseBody(session);
            Class paramClass = paramClassMap.get(method).get(uri);

            // Set the params
            AppiumParams appiumParams = (AppiumParams) paramClass.cast(gson.fromJson(postJson, paramClass));
            appiumParams.setSessionId(uriParams.get("sessionId"));
            appiumParams.setElementId(uriParams.get("elementId"));

            // Validate the sessionId
            if (appiumParams.getSessionId() != null && !appiumParams.getSessionId().equals(Session.getGlobalSessionId())) {
                return new ErrorResponse(AppiumStatus.BAD_PARAMETERS_ERROR);
            }

            // TODO: Add a try-catch block here to get appium exceptions and return proper status codes
            // Create the result
            Object handlerResult = handler.handle(appiumParams);
            String sessionId = appiumParams.getSessionId();
            if (handlerResult.getClass() == Session.class) {
                sessionId = ((Session) handlerResult).getId();
            }

            AppiumResponse appiumResponse = new AppiumResponse(AppiumStatus.SUCCESS, handlerResult, sessionId);
            System.out.println("Finished processing " + method + " request for '" + uri + "'");
            return appiumResponse;
        } else {
            return new ErrorResponse(AppiumStatus.UNKNOWN_COMMAND);
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
        } catch (ResponseException e) {
            // TODO: handle error
        }
        return result;
    }
}
