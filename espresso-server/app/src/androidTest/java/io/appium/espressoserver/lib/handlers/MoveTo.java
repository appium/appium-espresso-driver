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

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.MoveToParams;
import io.appium.espressoserver.lib.viewaction.ScrollTo;
import static android.support.test.espresso.action.ViewActions.scrollTo;

public class MoveTo implements RequestHandler<MoveToParams, Void> {

    @Override
    public Void handle(MoveToParams params) throws AppiumException {
        // Get a reference to the view and call onData. This will automatically scroll to the view.
        ViewInteraction viewInteraction = Element.getById(params.getElementId());

        try {
            // Try performing espresso's scrollTo, which will only work if
            //   1. View is descendant of scrollView
            //   2. View is visible
            viewInteraction.perform(scrollTo());
        } catch (PerformException pe) {
            // If it doesn't meet the above conditions, use our built-in scrollTo
            viewInteraction.perform(new ScrollTo());
        }

        return null;
    }


}
