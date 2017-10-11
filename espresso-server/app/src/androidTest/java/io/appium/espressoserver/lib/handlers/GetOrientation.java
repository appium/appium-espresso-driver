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

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class GetOrientation implements RequestHandler<AppiumParams, Integer> {

    @Override
    @Nullable
    public Integer handle(AppiumParams params) throws AppiumException {
        final ViewInteraction viewInteraction = Element.getById(params.getElementId());
        final View view = new ViewFinder().getView(viewInteraction);
        final Activity activity = new ViewElement(view).extractActivity();
        try {
            switch (activity.getRequestedOrientation()) {
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } catch (Exception e) {
            throw new AppiumException("Cannot get screen orientation", e);
        }
    }
}
