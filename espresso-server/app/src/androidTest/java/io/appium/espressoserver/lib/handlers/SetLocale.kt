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

import androidx.test.platform.app.InstrumentationRegistry
import io.appium.espressoserver.lib.helpers.ActivityHelpers
import io.appium.espressoserver.lib.helpers.changeLocale
import io.appium.espressoserver.lib.model.LocaleParams

class SetLocale : RequestHandler<LocaleParams, Void?> {

    override fun handleInternal(params: LocaleParams): Void? {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        changeLocale(instrumentation.targetContext.applicationContext, params.toLocale())
        val activity = ActivityHelpers.currentActivity
        val intent = activity.intent
        activity.finish()
        instrumentation.startActivitySync(intent)
        return null
    }

}
