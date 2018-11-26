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

package io.appium.espressoserver.lib.viewmatcher;

import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import androidx.test.espresso.Root;

public class ToastMatcher extends TypeSafeMatcher<Root> {
    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        if (root.getWindowLayoutParams().get().type != WindowManager.LayoutParams.TYPE_TOAST) {
            return false;
        }
        return root.getDecorView().getWindowToken() == root.getDecorView().getApplicationWindowToken();
    }
}
