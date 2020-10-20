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

package io.appium.espressoserver.lib.helpers

const val TAG = "appium"

object AndroidLogger : Logger {

    override fun error(vararg messages: Any) {
        android.util.Log.e(TAG, toString(*messages))
    }

    override fun error(message: String, throwable: Throwable) {
        android.util.Log.e(TAG, toString(message), throwable)
    }

    override fun info(vararg messages: Any) {
        android.util.Log.i(TAG, toString(*messages))
    }

    override fun debug(vararg messages: Any) {
        android.util.Log.d(TAG, toString(*messages))
    }

    override fun warn(vararg messages: Any) {
        android.util.Log.w(TAG, toString(*messages))
    }

    private fun toString(vararg args: Any): String =
            args.toList().joinToString(separator = "") { "$it" }
}
