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

package io.appium.espressoserver.lib.model.settings

import io.appium.espressoserver.lib.BuildConfig
import io.appium.espressoserver.lib.drivers.DriverContext
import io.appium.espressoserver.lib.handlers.exceptions.ComposeNotSupportedException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

class DriverSetting : AbstractSetting() {
    override var name: String = "driver"

    override fun value(): String {
        return DriverContext.currentStrategyType.name.lowercase()
    }

    override fun apply(value: Any?) {
        val requested = value?.toString()?.trim()?.lowercase()
        if (requested == DriverContext.StrategyType.COMPOSE.name.lowercase() && !BuildConfig.COMPOSE_SUPPORT) {
            throw ComposeNotSupportedException()
        }
        val driverStrategy =
            allowedStrategies().find { it.toString().lowercase() == requested }
                ?: throw InvalidArgumentException(
                    "driver type must be one of ${allowedStrategies().map { it.toString().lowercase() }}",
                )
        DriverContext.setDriverStrategy(driverStrategy)
    }

    private fun allowedStrategies(): List<DriverContext.StrategyType> =
        if (BuildConfig.COMPOSE_SUPPORT) {
            DriverContext.StrategyType.entries
        } else {
            listOf(DriverContext.StrategyType.ESPRESSO)
        }
}