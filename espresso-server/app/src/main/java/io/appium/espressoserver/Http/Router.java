package io.appium.espressoserver.Http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.Exceptions.DuplicateRouteException;
import io.appium.espressoserver.Handlers.Click;
import io.appium.espressoserver.Handlers.Finder;
import io.appium.espressoserver.Handlers.RequestHandler;


public class Router {
    private Map<Method, HashMap<String, RequestHandler>> routerMap;
    private Map<String, RequestHandler> regexRouterMap;

    public Router() throws DuplicateRouteException {
        routerMap = new HashMap<Method, HashMap<String, RequestHandler>>();
        addRoute(Method.GET, "/elements", new Finder());
        addRoute(Method.GET, "/elements/:id/click", new Click()); // TODO: Change this to POST later
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

    public AppiumResponse route(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        // TODO: Make this into a generic 404 Appium Response
        RequestHandler handler = new RequestHandler() {
            @Override
            public AppiumResponse handle(IHTTPSession session, Map<String, String> uriParams) {
                return null;
            }
        };

        // Get a matching handler
        String routeUri;
        Map<String, String> uriParams = new HashMap<String, String>();

        for (Map.Entry<String, RequestHandler> entry : routerMap.get(method).entrySet()) {
            String testUri = entry.getKey();
            String testRegex = "^";
            Map<Integer, String> wildcardIndices = new HashMap<Integer, String>();

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

        return handler.handle(session, uriParams);
    }
}
