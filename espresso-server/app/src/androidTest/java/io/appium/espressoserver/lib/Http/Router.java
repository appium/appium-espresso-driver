package io.appium.espressoserver.lib.Http;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.lib.Exceptions.DuplicateRouteException;
import io.appium.espressoserver.lib.Handlers.Click;
import io.appium.espressoserver.lib.Handlers.Finder;
import io.appium.espressoserver.lib.Handlers.RequestHandler;
import io.appium.espressoserver.lib.Handlers.CreateSession;
import io.appium.espressoserver.lib.Handlers.SendKeys;
import io.appium.espressoserver.lib.Handlers.Status;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.NotFoundResponse;
import io.appium.espressoserver.lib.Http.Response.InternalErrorResponse;


public class Router {
    private Map<Method, HashMap<String, RequestHandler>> routerMap;
    private Map<String, RequestHandler> regexRouterMap;

    public Router() throws DuplicateRouteException {
        routerMap = new HashMap<Method, HashMap<String, RequestHandler>>();

        addRoute(Method.POST, "/session", new CreateSession());
        addRoute(Method.GET, "/status", new Status());
        addRoute(Method.POST, "/sessions/:sessionId/elements", new Finder());
        addRoute(Method.POST, "/sessions/:sessionId/elements/:elementId/click", new Click());
        addRoute(Method.POST, "/sessions/:sessionId/elements/:elementId/value", new SendKeys());
    }

    private void addRoute(Method method, String uri, RequestHandler handler) throws DuplicateRouteException {
        if (!routerMap.containsKey(method)) {
            routerMap.put(method, new HashMap<String, RequestHandler>());
        }
        if (routerMap.get(method).containsKey(uri)) {
            throw new DuplicateRouteException("Duplicate router declaration for " + method.toString() + ": " + uri);
        }

        routerMap.get(method).put(uri, handler);
    }

    public BaseResponse route(IHTTPSession session) {
        RequestHandler handler;
        Map<String, String> uriParams;

        try {
            String uri = session.getUri();
            Method method = session.getMethod();

            if (!routerMap.containsKey(method)) {
                routerMap.put(method, new HashMap<String, RequestHandler>());
            }

            // By default, set handler to NotFound until we find a matching handler
            handler = new RequestHandler() {
                @Override
                public BaseResponse handle(IHTTPSession session, Map<String, String> uriParams) {
                    return new NotFoundResponse();
                }
            };

            // Get a matching handler
            uriParams = new HashMap<String, String>();

            // Look for a matching route
            // TODO: Move this to a separate method 'isRouteMatch'.
            for (Map.Entry<String, RequestHandler> entry : routerMap.get(method).entrySet()) {
                String testUri = entry.getKey();

                // TODO: Use StringBuilder to construct the Test Regexes
                String testRegex = "^";
                Map<Integer, String> wildcardIndices = new HashMap<Integer, String>();

                // Convert route to a regex to test incoming URI against
                // TODO: Cache these regexes instead of re-creating them every time
                int index = 0;
                for (String uriToken : testUri.split("/")) {
                    if (uriToken.startsWith(":")) {
                        testRegex += "/[\\w\\W]*";
                        wildcardIndices.put(index, uriToken.substring(1));
                    } else if (!uriToken.equals("")) {
                        testRegex += "/" + uriToken;
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

            return handler.handle(session, uriParams);
        } catch (Exception e) {
            // TODO: Don't show internal error messages in production, only show them in dev
            return new InternalErrorResponse(e.getMessage());
        }
    }
}
