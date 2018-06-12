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

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.helpers.w3c.processor.ActionsProcessor;
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;

@SuppressWarnings("unused")
public class Actions {
    private List<InputSource> actions = null;

    public static void performActions(List<Actions> actions, String sessionId)
            throws NotYetImplementedException, InvalidArgumentException {
        // Get state of session
        ActiveInputSources activeInputSources = ActiveInputSources.getActiveInputSourcesForSession(sessionId);
        InputStateTable inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId);

        // 1. Let actions by tick be the result of trying to extract an action sequence with argument parameters

        //ActionSequence actionSequence = new ActionSequence(actions, activeInputSources, inputStateTable);
    }

    public void releaseActions(String sessionId) {
        // Stub.
    }

    @Nullable
    public List<InputSource> getActions() {
        return actions;
    }

    public void setActions(List<InputSource> actions) {
        this.actions = actions;
    }

}
