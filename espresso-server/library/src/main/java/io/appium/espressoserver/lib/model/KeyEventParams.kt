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

import com.google.gson.annotations.SerializedName

data class KeyEventParams(
    @SerializedName("keycode")
    private val _keycode: Int? = null,
    @SerializedName("metastate")
    private val _metastate: Int? = null,
    @SerializedName("flags")
    private val _flags: Int? = null
) : AppiumParams() {
    val keycode: Int
        get() = _keycode ?: 0
    val metastate: Int
        get() = _metastate ?: 0
    val flags: Int
        get() = _flags ?: 0
}
