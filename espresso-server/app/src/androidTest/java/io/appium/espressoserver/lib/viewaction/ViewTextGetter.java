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

import android.support.test.espresso.ViewInteraction;
import android.view.View;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.model.ViewText;

public class ViewTextGetter {
    public ViewText get(ViewInteraction viewInteraction) throws AppiumException {
        final View view = new ViewGetter().getView(viewInteraction);
        final ViewText result = new ViewElement(view).getText();
        if (result == null) {
            throw new AppiumException(String.format("Views of class type %s have no 'text' property",
                    view.getClass().getName()));
        }
        return result;
    }
}
