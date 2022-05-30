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

import io.appium.espressoserver.lib.helpers.getSemanticsNode
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.Location
import io.appium.espressoserver.lib.model.ViewElement
import io.appium.espressoserver.lib.model.EspressoElement
import io.appium.espressoserver.lib.model.ComposeNodeElement

class GetLocation : RequestHandler<AppiumParams, Location> {

    override fun handleEspresso(params: AppiumParams): Location {
        val viewElement = ViewElement(EspressoElement.getViewById(params.elementId))
        return Location(viewElement.bounds.left, viewElement.bounds.top)
    }

    override fun handleCompose(params: AppiumParams): Location {
        val composeNodeElement = ComposeNodeElement(getSemanticsNode(params.elementId!!))
        return Location(composeNodeElement.bounds.left, composeNodeElement.bounds.top)
    }
}
