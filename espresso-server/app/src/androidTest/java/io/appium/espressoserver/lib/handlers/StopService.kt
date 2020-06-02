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

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import io.appium.espressoserver.lib.helpers.extractQualifiedClassName
import io.appium.espressoserver.lib.model.StopServiceParams

class StopService : RequestHandler<StopServiceParams, String?> {

    override fun handleInternal(params: StopServiceParams): String? {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(targetContext,
                Class.forName(extractQualifiedClassName(targetContext.packageName, params.intent)))
        if (!targetContext.stopService(intent)) {
            throw IllegalStateException("The '${params.intent}' service cannot be stopped " +
                    "or is unknown. Does the service belong to ${targetContext.packageName} app?")
        }

        return "true"
    }
}
