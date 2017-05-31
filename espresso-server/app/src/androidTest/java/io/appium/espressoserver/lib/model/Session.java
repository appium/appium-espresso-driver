package io.appium.espressoserver.lib.model;

import java.util.UUID;

import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException;


@SuppressWarnings("unused")
public class Session {
    // Only one session can run at a time so globally cache the current Session ID
    private static String ID;

    private final String id;

    private Session(String id) {
        // Instances of Session are private and only returned by 'createGlobalSession'
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static String getGlobalSessionId() {
        return Session.ID;
    }

    /**
     * Create a global session. Only one session can run per server instance so throw an exception if one already is in progress
     * @return Serializable Session object
     * @throws SessionNotCreatedException Thrown if a Session is already running
     */
    public static Session createGlobalSession() throws SessionNotCreatedException {
        if (Session.ID != null) {
            throw new SessionNotCreatedException(String.format("Session %s is already in progress. Appium Espresso Server can only handle one session at a time.", Session.ID));
        }
        Session.ID = UUID.randomUUID().toString();
        return new Session(Session.ID);
    }

    public static void deleteGlobalSession() {
        Session.ID = null;
    }
}
