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

package io.appium.espressoserver.lib.viewaction;

import android.content.pm.ActivityInfo;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

import io.appium.espressoserver.lib.model.ViewElement;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class OrientationChange implements ViewAction {
    private final int orientation;

    private OrientationChange(int orientation) {
        this.orientation = orientation;
    }

    public static ViewAction orientationLandscape() {
        return new OrientationChange(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public static ViewAction orientationPortrait() {
        return new OrientationChange(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public Matcher<View> getConstraints() {
        return isRoot();
    }

    @Override
    public String getDescription() {
        return "change orientation to " + orientation;
    }

    @Override
    public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        new ViewElement(view).extractActivity().setRequestedOrientation(orientation);
    }
}
