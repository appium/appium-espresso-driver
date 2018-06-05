package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tick implements Iterator<ActionObject> {

    private final List<ActionObject> actions = new ArrayList<>();
    private int actionCounter = 0;

    public void addAction(ActionObject action) {
        actions.add(action);
    }

    @Override
    public boolean hasNext() {
        return actionCounter < actions.size();
    }

    @Override
    public ActionObject next() {
        return actions.get(actionCounter++);
    }

    // TODO: Calculate Tick Duration (see 17.4)
    public long calculateTickDuration(){
        return -1;
    }

}
