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

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.DeviceInfoHelper
import io.appium.espressoserver.lib.model.AppiumParams
import java.util.*

class GetDeviceInfo : RequestHandler<AppiumParams, Map<String, Any?>> {
    @Throws(AppiumException::class)
    override fun handleInternal(params: AppiumParams): Map<String, Any?> {
        val deviceInfoHelper = DeviceInfoHelper(getApplicationContext<Context>())
        val result = HashMap<String, Any?>()
        result["androidId"] = deviceInfoHelper.androidId
        result["manufacturer"] = deviceInfoHelper.manufacturer
        result["model"] = deviceInfoHelper.modelName
        result["brand"] = deviceInfoHelper.brand
        result["apiVersion"] = deviceInfoHelper.apiVersion
        result["platformVersion"] = deviceInfoHelper.platformVersion
        result["carrierName"] = deviceInfoHelper.carrierName
        result["realDisplaySize"] = deviceInfoHelper.realDisplaySize
        result["displayDensity"] = deviceInfoHelper.displayDensity
        result["locale"] = deviceInfoHelper.locale
        result["timeZone"] = deviceInfoHelper.timeZone
        return result
    }
}
