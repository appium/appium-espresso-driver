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

import android.support.test.espresso.ViewInteraction;
import android.view.View;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.Rect;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class GetRect implements RequestHandler<AppiumParams, Rect> {

    @Override
    public Rect handle(AppiumParams params) throws AppiumException {
        final View view = Element.getViewById(params.getElementId());
        final ViewElement viewElement = new ViewElement(view);
        final Rect result = new Rect();
        final android.graphics.Rect elementBounds = viewElement.getBounds();
        result.setX(elementBounds.left);
        result.setY(elementBounds.top);
        result.setHeight(elementBounds.height());
        result.setWidth(elementBounds.width());
        return result;
    }
}
