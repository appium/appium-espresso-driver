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

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.InteractionHelper
import io.appium.espressoserver.lib.model.AppiumParams
import java.io.File

class UiautomatorPageSource : RequestHandler<AppiumParams, String> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: AppiumParams): String {
        var dumpXml = ""
        var dumpView: File? = null

        try {
            val uiDevice = InteractionHelper.getUiDevice()
            dumpView = File.createTempFile("window_dump", "uix")
            uiDevice.dumpWindowHierarchy(dumpView)
            dumpXml = dumpView.inputStream().readBytes().toString(Charsets.UTF_8)
            dumpView.delete()
        } catch (e: Exception) {
            throw AppiumException("Could not get page source with UiAutomator", e)
        } finally {
            dumpView?.delete()
        }
        return dumpXml
    }
}
