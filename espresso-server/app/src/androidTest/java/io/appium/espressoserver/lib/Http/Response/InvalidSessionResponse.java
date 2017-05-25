package io.appium.espressoserver.lib.Http.Response;

/**
 * Created by danielgraham on 5/25/17.
 */

public class InvalidSessionResponse extends BadRequestResponse {

    public InvalidSessionResponse (String sessionId) {
        super("Invalid Session ID: " + sessionId);
    }

}

