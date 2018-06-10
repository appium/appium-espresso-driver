package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;

import static io.appium.espressoserver.lib.helpers.w3c.models.W3CActions.processSourceActionSequence;

/**
 * The algorithm for extracting an action sequence from a request takes the JSON Object representing
 * an action sequence, validates the input, and returns a data structure that is the transpose of
 * the input JSON, such that the actions to be performed in a single tick are grouped together
 *
 * (Defined in 17.3 of spec 'extract an action sequence')
 */
public class ActionSequence implements Iterator<Tick> {

    private List<Tick> ticks = new ArrayList<>();
    private int tickCounter = 0;

    public ActionSequence(W3CActions w3CActions) throws InvalidArgumentException, NotYetImplementedException {
        // Check if null to keep Codacy happy. It will never make it this far if it's null though.
        if (w3CActions.getActions() != null) {
            for (InputSource inputSource : w3CActions.getActions()) {
                int tickIndex = 0;
                List<ActionObject> actionObjects = processSourceActionSequence(inputSource);
                for (ActionObject action : actionObjects) {
                    if (ticks.size() == tickIndex) {
                        ticks.add(new Tick());
                    }
                    Tick tick = ticks.get(tickIndex);
                    tick.addAction(action);
                    tickIndex++;
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return tickCounter < ticks.size();
    }

    @Override
    public Tick next() {
        return ticks.get(tickCounter++);
    }

    public void dispatch(W3CActionAdapter adapter) {
        for(Tick tick: ticks) {
            //tick.dispatch(adapter);
        }
    }
}
