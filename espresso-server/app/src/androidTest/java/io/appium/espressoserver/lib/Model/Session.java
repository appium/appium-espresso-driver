package io.appium.espressoserver.lib.Model;

import java.util.UUID;

/**
 * Created by danielgraham on 5/25/17.
 */

public class Session {
    private String id;

    // Only one session per test, keep this id globally
    private static String ID;

    public Session() {
        if (Session.ID != null) {
            this.id = Session.ID;
        } else {
            this.id = UUID.randomUUID().toString();
            Session.ID = this.id;
        }
    }

    public String getId () {
        return id;
    }

    public static String getGlobalSessionId() {
        return Session.ID;
    }
}
