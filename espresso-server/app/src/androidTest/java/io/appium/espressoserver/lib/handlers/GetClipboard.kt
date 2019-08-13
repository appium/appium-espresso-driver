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

import android.util.Base64

import java.nio.charset.StandardCharsets

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.ClipboardHelper
import io.appium.espressoserver.lib.model.ClipboardDataType
import io.appium.espressoserver.lib.model.GetClipboardParams

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation

class GetClipboard : RequestHandler<GetClipboardParams, String?> {
    private val mInstrumentation = getInstrumentation()

    @Throws(AppiumException::class)
    override fun handleInternal(params: GetClipboardParams): String? {
        try {
            return getClipboardResponse(params.contentType)
        } catch (e: IllegalArgumentException) {
            throw InvalidArgumentException(e)
        }
    }

    // Clip feature should run with main thread
    private fun getClipboardResponse(contentType: ClipboardDataType): String? {
        val runnable = GetClipboardRunnable(contentType)
        mInstrumentation.runOnMainSync(runnable)
        return runnable.content
    }

    private fun toBase64String(s: String): String {
        return Base64.encodeToString(s.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
    }

    private inner class GetClipboardRunnable internal constructor(private val contentType: ClipboardDataType) : Runnable {
        @Volatile
        var content: String? = null
            private set

        override fun run() {
            when (contentType) {
                ClipboardDataType.PLAINTEXT -> content = toBase64String(ClipboardHelper(mInstrumentation.targetContext).textData)
            }
        }
    }
}
