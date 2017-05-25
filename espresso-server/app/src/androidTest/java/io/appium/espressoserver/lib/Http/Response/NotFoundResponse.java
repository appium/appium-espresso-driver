package io.appium.espressoserver.lib.Http.Response;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by danielgraham on 5/25/17.
 */

public class NotFoundResponse extends AppiumResponse {
    public NotFoundResponse () {
        this.setStatus(NanoHTTPD.Response.Status.NOT_FOUND);
    }
}
