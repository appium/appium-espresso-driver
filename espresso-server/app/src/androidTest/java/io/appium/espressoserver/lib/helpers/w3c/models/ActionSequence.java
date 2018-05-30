package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;

/**
 * Pull out the actions from the input sources and group them by 'ticks'
 *
 * Defined in 17.3 of spec 'extract an action sequence'
 * @return
 */
public class ActionSequence implements Iterator<Tick> {

    private List<Tick> actionsByTick;
    private int tickCounter;

    public ActionSequence(W3CActions w3CActions) throws InvalidArgumentException{
        tickCounter = 0;
        actionsByTick = new ArrayList<>();

        // Transpose the actionsByTick within the input sources
        if (w3CActions.getActions() != null) {
            for (InputSource inputSource : w3CActions.getActions()) {
                int i = 0;
                for (Action action : inputSource.getActions()) {
                    if (actionsByTick.size() == i) {
                        actionsByTick.add(new Tick());
                    }
                    actionsByTick.get(i).addAction(action);
                    i++;
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return tickCounter < actionsByTick.size();
    }

    @Override
    public Tick next() {
        return actionsByTick.get(tickCounter++);
    }
}
