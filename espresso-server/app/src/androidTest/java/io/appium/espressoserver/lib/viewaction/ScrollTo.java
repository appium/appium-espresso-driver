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

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.widget.AbsListView;

import org.hamcrest.Matcher;

import javax.annotation.Nullable;

import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class ScrollTo implements ViewAction {

    private int xOffset = 0;
    private int yOffset = 0;

    public ScrollTo() {
        super();
    }

    public ScrollTo(@Nullable Integer xOffset, @Nullable Integer yOffset) {
        super();
        if (xOffset != null) {
            this.xOffset = xOffset;
        }
        if (yOffset != null) {
            this.yOffset = yOffset;
        }
    }

    @Override
    public Matcher<View> getConstraints() {
        // This is a hack constraint that passes any view through
        return isDescendantOfA(isRoot());
    }

    @Override
    public String getDescription() {
        return "getting a view reference";
    }

    @Override
    public void perform(UiController uiController, View view) {
        int x = view.getLeft() + this.xOffset;
        int y = view.getTop() + view.getHeight() + this.yOffset;

        View viewParent = (View) view.getParent();

        if (viewParent instanceof AbsListView) {
            ((AbsListView) viewParent).smoothScrollToPosition(y);
        } else {
            viewParent.scrollTo(x, y);
        }
    }

}
