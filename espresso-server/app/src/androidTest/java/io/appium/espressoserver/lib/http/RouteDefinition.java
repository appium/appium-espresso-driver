package io.appium.espressoserver.lib.http;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.Method;
import io.appium.espressoserver.lib.handlers.RequestHandler;
import io.appium.espressoserver.lib.model.AppiumParams;

class RouteDefinition {

    private final String testRegex;
    private final String routeUri;
    private final Method method;
    private final Class<? extends AppiumParams> paramClass;
    private final RequestHandler<? extends AppiumParams, ?> handler;

    RouteDefinition(Method method, String routeUri, RequestHandler<? extends AppiumParams, ?> handler, Class<? extends AppiumParams> paramClass) {
        testRegex = RouteDefinition.buildTestRegex(routeUri);
        this.routeUri = routeUri;
        this.method = method;
        this.paramClass = paramClass;
        this.handler = handler;
    }

    public boolean isMatch (String uri) {
        return uri.matches(testRegex);
    }

    public Map<String, String> getUriParams(String uri) {
        Map<String, String> uriParams = new HashMap<>();
        String[] uriTokens = uri.split("/");

        int index = 0;
        for (String routeUriToken : routeUri.split("/")) {
            // If a token starts with ':', then what's after is an identifier
            if (routeUriToken.startsWith(":")) {
                uriParams.put(routeUriToken.substring(1), uriTokens[index]);
            }
            index++;
        }
        return uriParams;
    }

    public Class<? extends AppiumParams> getParamClass() {
        return paramClass;
    }

    public Method getMethod () {
        return method;
    }

    public RequestHandler<? extends AppiumParams, ?> getHandler() {
        return handler;
    }

    public String getRouteUri () {
        return routeUri;
    }

    private static String buildTestRegex(String uri) {
        StringBuilder testRegex = new StringBuilder("^");

        // Convert route to a regex
        for (String uriToken : uri.split("/")) {
            if (uriToken.startsWith(":")) {
                testRegex.append("/[\\w\\W]*");
            } else if (!uriToken.isEmpty()) {
                testRegex.append("/");
                testRegex.append(uriToken);
            }
        }
        testRegex.append("$");
        return testRegex.toString();
    }
}
