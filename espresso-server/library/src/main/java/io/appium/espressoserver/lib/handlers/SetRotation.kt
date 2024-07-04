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

import android.os.SystemClock
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.helpers.InteractionHelper
import io.appium.espressoserver.lib.helpers.InteractionHelper.getUiAutomation
import io.appium.espressoserver.lib.model.RotationParams
import java.time.Duration

val ROTATION_CHANGE_TIMEOUT: Duration = Duration.ofSeconds(5)

class SetRotation : RequestHandler<RotationParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: RotationParams): Void? {
        val desiredRotation = params.validate().z!! / 90
        if (getUiAutomation().setRotation(desiredRotation)) {
            val start = SystemClock.currentThreadTimeMillis()
            do {
                if (InteractionHelper.getUiDevice().displayRotation == desiredRotation) {
                    return null
                }
                SystemClock.sleep(100)
            } while (SystemClock.currentThreadTimeMillis() - start < ROTATION_CHANGE_TIMEOUT.toMillis())
        }
        throw InvalidElementStateException(
                "The display rotation cannot be changed to ${params.z} degrees after $ROTATION_CHANGE_TIMEOUT")
    }
}
