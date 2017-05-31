package io.appium.espressoserver.lib.http;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import fi.iki.elonen.NanoHTTPD.Method;


class RouteMap {

    private final Map<Method, Map<String, RouteDefinition>> routeMap = new HashMap<>();

    void addRoute(RouteDefinition route) {
        if (!routeMap.containsKey(route.getMethod())) {
            routeMap.put(route.getMethod(), new HashMap<String, RouteDefinition>());
        }
        Map<String, RouteDefinition> methodMap = routeMap.get(route.getMethod());
        methodMap.put(route.getRouteUri(), route);
    }

    @Nullable
    public RouteDefinition findMatchingRoute(String uri) {
        for (Map.Entry<Method, Map<String, RouteDefinition>> methodMap: routeMap.entrySet()) {
           for (Map.Entry<String, RouteDefinition> route: methodMap.getValue().entrySet()) {
               RouteDefinition routeDefinition = route.getValue();
               if (routeDefinition.isMatch(uri)) {
                   return routeDefinition;
               }
           }
        }

        return null;
    }

}
