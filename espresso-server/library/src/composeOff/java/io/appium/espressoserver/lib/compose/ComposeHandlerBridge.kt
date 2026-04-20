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

package io.appium.espressoserver.lib.compose

import io.appium.espressoserver.lib.handlers.exceptions.ComposeNotSupportedException
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.BaseElement
import io.appium.espressoserver.lib.model.Locator
import io.appium.espressoserver.lib.model.Location
import io.appium.espressoserver.lib.model.MobileSwipeParams
import io.appium.espressoserver.lib.model.Rect
import io.appium.espressoserver.lib.model.Size
import io.appium.espressoserver.lib.model.TextValueParams

internal object ComposeHandlerBridge {
    private fun unsupported(): Nothing = throw ComposeNotSupportedException()

    fun findElement(params: Locator): BaseElement = unsupported()

    fun findElements(params: Locator): List<BaseElement> = unsupported()

    fun click(params: AppiumParams) {
        unsupported()
    }

    fun clear(params: AppiumParams) {
        unsupported()
    }

    fun elementValue(params: TextValueParams, isReplacing: Boolean) {
        unsupported()
    }

    fun keys(params: TextValueParams) {
        unsupported()
    }

    fun mobileSwipe(params: MobileSwipeParams): Void? = unsupported()

    fun getDisplayed(params: AppiumParams): Boolean = unsupported()

    fun elementScreenshot(elementId: String): String = unsupported()

    fun getAttribute(elementId: String, attributeName: String): String? = unsupported()

    fun getEnabled(params: AppiumParams): Boolean = unsupported()

    fun getLocation(params: AppiumParams): Location = unsupported()

    fun getName(params: AppiumParams): String? = unsupported()

    fun getRect(params: AppiumParams): Rect = unsupported()

    fun getSelected(params: AppiumParams): Boolean = unsupported()

    fun getSize(params: AppiumParams): Size = unsupported()

    fun text(params: AppiumParams): String = unsupported()
}
