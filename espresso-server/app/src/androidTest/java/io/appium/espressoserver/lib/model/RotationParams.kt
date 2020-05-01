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

package io.appium.espressoserver.lib.model

import kotlin.IllegalArgumentException

val SUPPORTED_Z_VALUES = listOf(0, 90, 180, 270)

data class RotationParams(
    val x: Int?,
    val y: Int?,
    val z: Int?
) : AppiumParams() {
    fun validate(): RotationParams {
        if (z == null) {
            throw IllegalArgumentException("z argument must be provided")
        }

        if (!SUPPORTED_Z_VALUES.contains(z)) {
            throw IllegalArgumentException("z argument value must be one of $SUPPORTED_Z_VALUES")
        }
        return this
    }
}
