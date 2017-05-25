package io.appium.espressoserver.Http;

import java.util.HashMap;

import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.Exceptions.DuplicateRouteException;
import io.appium.espressoserver.Handlers.Finder;
import io.appium.espressoserver.Handlers.RequestHandler;


public class Router {
    private HashMap<Method, HashMap<String, RequestHandler>> routerMap;

    public Router() throws DuplicateRouteException {
        routerMap = new HashMap<Method, HashMap<String, RequestHandler>>();
        addRoute(Method.GET, "/elements", new Finder());
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

        RequestHandler handler = routerMap.get(method).get(uri);
        return handler.handle(session);
    }
}
