/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
