/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.helpers.w3c.models;

import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;

@SuppressWarnings("unused")
public class Actions {
    private List<InputSource> actions;
    private W3CActionAdapter adapter;

    /**
     * Perform actions (17.5)
     * @param sessionId ID of the session to perform actions on
     * @throws AppiumException
     */
    public void performActions(String sessionId) throws AppiumException {

        if (adapter == null) {
            throw new AppiumException("An internal server error has occurred: Failed to initialize /actions adapter");
        }

        // Get state of session
        ActiveInputSources activeInputSources = ActiveInputSources.getActiveInputSourcesForSession(sessionId);
        InputStateTable inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId);

        // Let `actions by tick` be the result of trying to extract an action sequence with argument parameters
        ActionSequence actionsByTick = new ActionSequence(this, activeInputSources, inputStateTable);
        try {
            // Dispatch the actions
            actionsByTick.dispatch(adapter, inputStateTable);
        } catch (InterruptedException e) {
            throw new AppiumException(e.getCause().toString());
        } catch (ExecutionException e) {
            throw new AppiumException(e.getCause().toString());
        }
    }

    /**
     * Release actions (17.6)
     * @param sessionId ID of the session to release actions on
     */
    public void releaseActions(String sessionId) throws AppiumException {
        if (adapter == null) {
            throw new AppiumException("An internal server error has occurred: Failed to initialize /actions adapter");
        }

        // Get state of session
        InputStateTable inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId);

        // Undo all actions
        inputStateTable.undoAll(adapter, System.currentTimeMillis());
    }

    public void setAdapter(W3CActionAdapter adapter) {
        this.adapter = adapter;
    }

    public W3CActionAdapter getAdapter() {
        return adapter;
    }

    @Nullable
    public List<InputSource> getActions() {
        return actions;
    }

    public void setActions(List<InputSource> actions) {
        this.actions = actions;
    }

}
