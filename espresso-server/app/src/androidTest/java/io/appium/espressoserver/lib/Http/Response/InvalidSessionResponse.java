package io.appium.espressoserver.lib.Http.Response;

public class InvalidSessionResponse extends BadRequestResponse {

    public InvalidSessionResponse (String sessionId) {
        super("Invalid Session ID: " + sessionId);
    }

}

