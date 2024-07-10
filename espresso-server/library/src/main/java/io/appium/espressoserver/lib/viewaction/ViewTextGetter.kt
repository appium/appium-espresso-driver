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

package io.appium.espressoserver.lib.viewaction

import androidx.test.espresso.ViewInteraction
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.ViewElement
import io.appium.espressoserver.lib.model.ViewText

class ViewTextGetter {
    @Throws(AppiumException::class)
    operator fun get(viewInteraction: ViewInteraction): ViewText {
        val view = ViewGetter().getView(viewInteraction)
        return ViewElement(view).text
                ?: throw AppiumException("Views of class type ${view.javaClass.name} have no 'text' property")
    }
}
