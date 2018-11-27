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

import androidx.test.espresso.ViewInteraction;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.MobileSwipeParams;

import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeUp;

public class MobileSwipe implements RequestHandler<MobileSwipeParams, Void> {

    @Override
    public Void handle(final MobileSwipeParams params) throws AppiumException {
        if (params.getDirection() == null) {
            throw new InvalidArgumentException("Direction must not be empty and must be of type: UP, DOWN, LEFT or RIGHT");
        }

        // Get a reference to the view and call onData. This will automatically scroll to the view.
        ViewInteraction viewInteraction = Element.getViewInteractionById(params.getElementId());

        switch (params.getDirection()) {
            case UP:
                viewInteraction.perform(swipeUp());
                break;
            case DOWN:
                viewInteraction.perform(swipeDown());
                break;
            case LEFT:
                viewInteraction.perform(swipeLeft());
                break;
            case RIGHT:
                viewInteraction.perform(swipeRight());
                break;
            default:
                throw new InvalidArgumentException(String.format("Direction cannot be %s", params.getDirection()));
        }
        return null;
    }
}
