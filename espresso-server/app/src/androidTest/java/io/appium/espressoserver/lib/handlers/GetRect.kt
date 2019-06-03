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

package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.Rect
import io.appium.espressoserver.lib.model.ViewElement

class GetRect : RequestHandler<AppiumParams, Rect> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: AppiumParams): Rect {
        val viewElement = ViewElement(Element.getViewById(params.elementId))
        val elementBounds = viewElement.bounds
        return Rect(
            elementBounds.left,
            elementBounds.top,
            elementBounds.width(),
            elementBounds.height()
        )
    }
}
