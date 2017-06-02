package io.appium.espressoserver.lib.model;

import java.util.UUID;

import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException;
import io.appium.espressoserver.lib.model.SessionParams.DesiredCapabilities;


@SuppressWarnings("unused")
public class Session {
    // Only one session can run at a time so globally cache the current Session ID
    private static volatile Session globalSession;

    private final String id;
    private final DesiredCapabilities desiredCapabilities;

    private Session(String id, DesiredCapabilities desiredCapabilities) {
        // Instances of Session are private and only returned by 'createGlobalSession'
        this.id = id;
        this.desiredCapabilities = desiredCapabilities;
    }

    public String getId() {
        return id;
    }

    public DesiredCapabilities getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public static Session getGlobalSession() {
        return globalSession;
    }

    /**
     * Create a global session. Only one session can run per server instance so throw an exception if one already is in progress
     * @return Serializable Session object
     * @throws SessionNotCreatedException Thrown if a Session is already running
     */
    public synchronized static Session createGlobalSession(DesiredCapabilities desiredCapabilities) throws SessionNotCreatedException {
        if (globalSession != null) {
            throw new SessionNotCreatedException(String.format("Session %s is already in progress. Appium Espresso Server can only handle one session at a time.", globalSession.getId()));
        }
        String globalSessionId = UUID.randomUUID().toString();
        Session.globalSession = new Session(globalSessionId, desiredCapabilities);
        return Session.globalSession;
    }

    public static void deleteGlobalSession() {
        Session.globalSession = null;
    }
}
