package io.appium.espressoserver.lib.helpers.w3c.models;

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tick implements Iterator<Action> {

    private final List<Action> actions;
    private int actionCounter;

    public Tick() {
        actions = new ArrayList<>();
        actionCounter = 0;
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    @Override
    public boolean hasNext() {
        return actionCounter < actions.size();
    }

    @Override
    public Action next() {
        return actions.get(actionCounter++);
    }

    // TODO: Calculate Tick Duration (see 17.4)
    public long calculateTickDuration(){
        return -1;
    }

}
