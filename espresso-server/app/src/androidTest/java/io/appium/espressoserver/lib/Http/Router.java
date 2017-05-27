package io.appium.espressoserver.lib.Http;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.lib.Exceptions.DuplicateRouteException;
import io.appium.espressoserver.lib.Handlers.Click;
import io.appium.espressoserver.lib.Handlers.Finder;
import io.appium.espressoserver.lib.Handlers.RequestHandler;
import io.appium.espressoserver.lib.Handlers.CreateSession;
import io.appium.espressoserver.lib.Handlers.DeleteSession;
import io.appium.espressoserver.lib.Handlers.SendKeys;
import io.appium.espressoserver.lib.Handlers.Status;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.NotFoundResponse;
import io.appium.espressoserver.lib.Http.Response.InternalErrorResponse;


class Router {
    private final Map<Method, HashMap<String, RequestHandler>> routerMap;

    Router() throws DuplicateRouteException {
        routerMap = new HashMap<>();

        addRoute(Method.POST, "/session", new CreateSession());
        addRoute(Method.DELETE, "/session/:sessionId", new DeleteSession());
        addRoute(Method.GET, "/status", new Status());
        addRoute(Method.POST, "/session/:sessionId/element", new Finder());
        addRoute(Method.POST, "/session/:sessionId/element/:elementId/click", new Click());
        addRoute(Method.POST, "/session/:sessionId/element/:elementId/value", new SendKeys());
    }

    private void addRoute(Method method, String uri, RequestHandler handler) throws DuplicateRouteException {
        if (!routerMap.containsKey(method)) {
            routerMap.put(method, new HashMap<String, RequestHandler>());
        }
        if (routerMap.get(method).containsKey(uri)) {
            throw new DuplicateRouteException();
        }

        routerMap.get(method).put(uri, handler);
    }

    BaseResponse route(IHTTPSession session) {
        RequestHandler handler;

        try {
            String uri = session.getUri();
            Method method = session.getMethod();

            System.out.println("Received " + method + " request for '" + uri + "'");

            if (!routerMap.containsKey(method)) {
                routerMap.put(method, new HashMap<String, RequestHandler>());
            }

            // By default, set handler to NotFound until we find a matching handler
            handler = new RequestHandler() {
                @Override
                public BaseResponse handle(IHTTPSession session, Map<String, Object> params) {
                    return new NotFoundResponse();
                }
            };

            Map<String, Object> params = parseBody(session);

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
                        params.put(wildcardIndexEntry.getValue(), uriTokens[wildcardIndex]);
                    }
                    handler = routerMap.get(method).get(entry.getKey());
                    break;
                }
            }

            BaseResponse res = handler.handle(session, params);
            System.out.println("Finished processing " + method + " request for '" + uri + "'");
            return res;
        } catch (Exception e) {
            // TODO: Don't show internal error messages in production, only show them in dev
            return new InternalErrorResponse(e.getMessage());
        }
    }

    private Map<String, Object> parseBody (IHTTPSession session) {
        Map<String, Object> result = new HashMap();
        try {
            Map<String, String> files = new HashMap();
            session.parseBody(files);

            Gson gson = new Gson();
            result = gson.fromJson(files.get("postData"), Map.class);
        } catch (Exception e) {
            // TODO: error handling
        }

        return result;
    }
}
