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
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.Espresso.onView;

import java.util.Arrays;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.OrientationParams;
import io.appium.espressoserver.lib.viewaction.OrientationChange;

public class SetOrientation implements RequestHandler<OrientationParams, Void> {

    @Override
    @Nullable
    public Void handle(OrientationParams params) throws AppiumException {
        final String elementId = params.getElementId();
        final ViewInteraction viewInteraction = elementId == null
                ? onView(isRoot()) 
                : Element.getViewInteractionById(elementId);
        final String orientation = params.getOrientation();
        if (orientation == null ||
                !Arrays.asList(new String[] {"LANDSCAPE", "PORTRAIT"}).
                        contains(orientation.toUpperCase())) {
            throw new AppiumException(String.format("Screen orientation value to '%s'",
                    orientation));
        }
        try {
            if (orientation.equalsIgnoreCase("LANDSCAPE")) {
                viewInteraction.perform(OrientationChange.orientationLandscape());
            } else {
                viewInteraction.perform(OrientationChange.orientationPortrait());
            }
        } catch (Exception e) {
            throw new AppiumException(String.format("Cannot change screen orientation to '%s'",
                    orientation), e);
        }
        return null;
    }
}
