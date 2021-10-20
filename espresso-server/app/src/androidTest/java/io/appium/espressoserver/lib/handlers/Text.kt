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

import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.helpers.getSemanticsNode
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.ComposeNodeElement
import io.appium.espressoserver.lib.model.EspressoElement
import io.appium.espressoserver.lib.viewaction.ViewTextGetter

class Text : RequestHandler<AppiumParams, String> {

    override fun handleEspresso(params: AppiumParams): String {
        val viewInteraction = EspressoElement.getViewInteractionById(params.elementId)
        return ViewTextGetter()[viewInteraction].rawText
    }

    override fun handleCompose(params: AppiumParams): String =
        ComposeNodeElement(getSemanticsNode(params.elementId!!)).text.toString()
}
