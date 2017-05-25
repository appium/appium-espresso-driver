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
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.NotFoundResponse;
import io.appium.espressoserver.lib.Http.Response.InternalErrorResponse;


public class Router {
    private Map<Method, HashMap<String, RequestHandler>> routerMap;
    private Map<String, RequestHandler> regexRouterMap;

    public Router() throws DuplicateRouteException {
        routerMap = new HashMap<Method, HashMap<String, RequestHandler>>();

        addRoute(Method.POST, "/session", new CreateSession()); // TODO: Change this to POST
        addRoute(Method.POST, "/sessions/:sessionId/elements", new Finder());
        addRoute(Method.POST, "/elements/:sessionId/click", new Click()); // TODO: Change this to POST later
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
            for (Map.Entry<String, RequestHandler> entry : routerMap.get(method).entrySet()) {
                String testUri = entry.getKey();
                String testRegex = "^";
                Map<Integer, String> wildcardIndices = new HashMap<Integer, String>();

                // Convert route to a regex to test incoming URI against
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
                    String[] uriTokens = uri.split("/");
                    for (Map.Entry<Integer, String> wildcardIndexEntry : wildcardIndices.entrySet()) {
                        int wildcardIndex = wildcardIndexEntry.getKey();
                        uriParams.put(wildcardIndexEntry.getValue(), uriTokens[wildcardIndex]);
                    }
                    handler = routerMap.get(method).get(entry.getKey());
                    break;
                }
            }
        } catch (Exception e) {
            return new InternalErrorResponse(e.getMessage());
        }

        return handler.handle(session, uriParams);
    }
}
