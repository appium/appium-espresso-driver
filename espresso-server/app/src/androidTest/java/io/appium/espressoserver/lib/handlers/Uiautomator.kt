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

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.ArrayList

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.ReflectionUtils
import io.appium.espressoserver.lib.model.UiautomatorParams

import io.appium.espressoserver.lib.helpers.AndroidLogger.logger
import io.appium.espressoserver.lib.helpers.InteractionHelper.getUiDevice

class Uiautomator : RequestHandler<UiautomatorParams, List<Any>> {

    @Throws(AppiumException::class)
    override fun handle(params: UiautomatorParams): List<Any> {
        logger.info("Invoking Uiautomator2 Methods")

        val validStrategyNames = UiautomatorParams.Strategy.getValidStrategyNames();
        params.strategy ?: throw AppiumException("strategy should be one of '${validStrategyNames}'")

        val validActionNames = UiautomatorParams.Action.getValidActionNames()
        params.action ?: throw AppiumException("strategy should be one of '${validActionNames}'")

        val locator = params.locator
        val index = params.index

        try {
            val byMethod = ReflectionUtils.method(By::class.java, params.strategy!!.getName(), String::class.java)
            val bySelector = ReflectionUtils.invoke(byMethod, By::class.java, locator) as BySelector
            val uiObjectMethod = ReflectionUtils.method(UiObject2::class.java, params.action!!.getName())
            val uiObjects = getUiDevice().findObjects(bySelector)
            logger.info("Found ${uiObjects.size} UiObjects", uiObjects.size)

            index ?: run {
                return uiObjects.map {
                    uiObjectMethod.invoke(it)
                }
            }

            if (index >= uiObjects.size) {
                throw AppiumException(
                        String.format("Index %d is out of bounds for %d elements", index, uiObjects.size))
            }

            return listOf(uiObjectMethod.invoke(uiObjects[index]))
        } catch (e: IllegalAccessException) {
            throw AppiumException(e)
        } catch (e: InvocationTargetException) {
            throw AppiumException(e)
        }

    }
}
