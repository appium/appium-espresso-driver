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
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.InteractionHelper.getUiDevice
import io.appium.espressoserver.lib.helpers.KReflectionUtils
import io.appium.espressoserver.lib.model.UiautomatorParams
import java.lang.reflect.InvocationTargetException

class Uiautomator : RequestHandler<UiautomatorParams, List<Any?>> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: UiautomatorParams): List<Any?> {
        AndroidLogger.logger.info("Invoking Uiautomator2 Methods")

        val validStrategyNames = UiautomatorParams.Strategy.validStrategyNames
        params.strategy ?: throw AppiumException("strategy should be one of '${validStrategyNames}'")

        val validActionNames = UiautomatorParams.Action.validActionNames
        params.action ?: throw AppiumException("strategy should be one of '${validActionNames}'")

        val locator = params.locator
        val index = params.index

        try {
            /*val byMethod = ReflectionUtils.method(By::class.java, params.strategy.name, String::class.java)
            val bySelector = ReflectionUtils.invoke(byMethod, By::class.java, locator) as BySelector
            val uiObjectMethod = ReflectionUtils.method(UiObject2::class.java, params.action.name)*/

            val bySelector = KReflectionUtils.invokeMethod(By::class, params.strategy.methodName, locator) as BySelector
            val uiObjects = getUiDevice().findObjects(bySelector)
            AndroidLogger.logger.info("Found ${uiObjects.size} UiObjects", uiObjects.size)

            index ?: run {
                return uiObjects.map {
                    KReflectionUtils.invokeInstanceMethod(it, params.action.actionName)
                }
            }

            if (index >= uiObjects.size) {
                throw AppiumException("Index $index is out of bounds for ${uiObjects.size} elements")
            }

            return listOf(KReflectionUtils.invokeInstanceMethod(uiObjects[index], params.action.actionName))
        } catch (e: IllegalAccessException) {
            throw AppiumException(e)
        } catch (e: InvocationTargetException) {
            throw AppiumException(e)
        }

    }
}
