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

package io.appium.espressoserver.lib.handlers;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

import java.util.HashMap;
import java.util.Map;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.DeviceInfoHelper;
import io.appium.espressoserver.lib.model.AppiumParams;

public class GetDeviceInfo implements RequestHandler<AppiumParams, Map<String, Object>> {
    private final Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();

    @Override
    public Map<String, Object> handle(AppiumParams params) throws AppiumException {
        final DeviceInfoHelper deviceInfoHelper = new DeviceInfoHelper(mInstrumentation.getTargetContext());
        final Map<String, Object> result = new HashMap<>();
        result.put("androidId", deviceInfoHelper.getAndroidId());
        result.put("manufacturer", deviceInfoHelper.getManufacturer());
        result.put("model", deviceInfoHelper.getModelName());
        result.put("brand", deviceInfoHelper.getBrand());
        result.put("apiVersion", deviceInfoHelper.getApiVersion());
        result.put("platformVersion", deviceInfoHelper.getPlatformVersion());
        result.put("carrierName", deviceInfoHelper.getCarrierName());
        result.put("realDisplaySize", deviceInfoHelper.getRealDisplaySize());
        result.put("displayDensity", deviceInfoHelper.getDisplayDensity());
        return result;
    }
}
