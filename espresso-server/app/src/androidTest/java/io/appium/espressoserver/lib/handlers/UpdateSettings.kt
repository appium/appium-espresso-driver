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

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.model.SettingsParams
import io.appium.espressoserver.lib.model.settings.AbstractSetting
import io.appium.espressoserver.lib.model.settings.SettingType

class UpdateSettings : RequestHandler<SettingsParams, Void?> {
    override fun handleInternal(params: SettingsParams): Void? =
        params.settings.map { getSetting(it.key).apply(it.value) }.let { return null }

    private fun getSetting(settingName: String): AbstractSetting =
        SettingType.values().find { it.setting.name == settingName }?.setting
            ?: throw InvalidArgumentException(
                "Could not find matching setting. Known setting names are: ${
                    SettingType.values().map { it.toString().lowercase() }
                }"
            )
}