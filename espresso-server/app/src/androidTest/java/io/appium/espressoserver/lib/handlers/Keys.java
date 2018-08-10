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

package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.UiController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions.ActionsBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceBuilder;
import io.appium.espressoserver.lib.model.TextParams;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY;

public class Keys implements RequestHandler<TextParams, Void> {

    @Override
    public Void handle(final TextParams params) throws AppiumException {
        UiControllerRunnable<Void> runnable = new UiControllerRunnable<Void>() {
            @Override
            public Void run(UiController uiController) throws AppiumException {
                // Add a list of keyDown + keyUp actions for each key
                List<Action> keyActions = new ArrayList<>();
                for (final String key:params.getValue()) {
                    keyActions.add(new ActionBuilder()
                        .withType(KEY_DOWN)
                        .withValue(key)
                        .build()
                    );

                    // Key up event
                    keyActions.add(new ActionBuilder()
                            .withType(KEY_UP)
                            .withValue(key)
                            .build()
                    );
                }

                InputSource keyInputSource = new InputSourceBuilder()
                        .withId("keyboard")
                        .withType(KEY)
                        .withActions(keyActions)
                        .build();

                Actions actions = new ActionsBuilder()
                        .withActions(Collections.singletonList(keyInputSource))
                        .withAdapter(new EspressoW3CActionAdapter(uiController))
                        .build();

                actions.perform(params.getSessionId());
                actions.release(params.getSessionId());

                return null;
            }
        };

        new UiControllerPerformer<>(runnable).run();
        return null;
    }
}
