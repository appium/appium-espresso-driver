package io.appium.espressoserver.lib.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import fi.iki.elonen.NanoHTTPD.Method;
import io.appium.espressoserver.lib.handlers.exceptions.DuplicateRouteException;


public class RouteMap {

    private final Map<Method, Map<String, RouteDefinition>> routeMap = new ConcurrentHashMap<>();

    public void addRoute(RouteDefinition route) {
        if (!routeMap.containsKey(route.getMethod())) {
            routeMap.put(route.getMethod(), new ConcurrentHashMap<String, RouteDefinition>());
        }
        Map<String, RouteDefinition> methodMap = routeMap.get(route.getMethod());
        if (methodMap.containsKey(route.getRouteUri())) {
            throw new DuplicateRouteException();
        }
        methodMap.put(route.getRouteUri(), route);
    }

    @Nullable
    public RouteDefinition findMatchingRoute(Method method, String uri) {
        if (!routeMap.containsKey(method)) {
            return null;
        }
        Map<String, RouteDefinition> methodMap = routeMap.get(method);
        for (Map.Entry<String, RouteDefinition> route: methodMap.entrySet()) {
           RouteDefinition routeDefinition = route.getValue();
           if (routeDefinition.isMatch(uri)) {
               return routeDefinition;
           }
        }

        return null;
    }

}
