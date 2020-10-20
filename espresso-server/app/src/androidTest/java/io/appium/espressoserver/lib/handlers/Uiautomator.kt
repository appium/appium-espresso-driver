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

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.InteractionHelper.getUiDevice
import io.appium.espressoserver.lib.helpers.ReflectionUtils.extractMethod
import io.appium.espressoserver.lib.helpers.ReflectionUtils.invokeMethod
import io.appium.espressoserver.lib.model.UiautomatorParams
import java.lang.reflect.InvocationTargetException

class Uiautomator : RequestHandler<UiautomatorParams, List<Any?>> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: UiautomatorParams): List<Any?> {
        val validStrategyNames = UiautomatorParams.Strategy.values().map { it.strategyName }
        params.strategy ?: throw AppiumException("strategy should be one of '${validStrategyNames}'")

        val validActionNames = UiautomatorParams.Action.values().map { it.actionName }
        params.action ?: throw AppiumException("action should be one of '${validActionNames}'")

        val locator = params.locator
        val index = params.index

        try {
            val byMethod = extractMethod(By::class.java, params.strategy.strategyName, String::class.java)
            val bySelector = invokeMethod(null, byMethod, locator) as BySelector
            val actionMethod = extractMethod(UiObject2::class.java, params.action.actionName)

            val uiObjects = getUiDevice().findObjects(bySelector)
            AndroidLogger.info("Found ${uiObjects.size} UiObject(s)")

            index ?: run {
                return uiObjects.map {
                    invokeMethod(it, actionMethod)
                }
            }

            if (index >= uiObjects.size) {
                throw AppiumException("Index $index is out of bounds for ${uiObjects.size} elements")
            }

            return listOf(invokeMethod(uiObjects[index], actionMethod))
        } catch (e: IllegalAccessException) {
            throw AppiumException(e)
        } catch (e: InvocationTargetException) {
            throw AppiumException(e)
        }
    }
}
