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
import io.appium.espressoserver.lib.model.SetClipboardParams

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation

class SetClipboard : RequestHandler<SetClipboardParams, Void?> {
    private val mInstrumentation = getInstrumentation()

    @Throws(AppiumException::class)
    override fun handleInternal(params: SetClipboardParams): Void? {
        params.content ?: throw InvalidArgumentException("The 'content' argument is mandatory")
        try {
            mInstrumentation.runOnMainSync(SetClipboardRunnable(
                    params.contentType, params.label, fromBase64String(params.content)))
        } catch (e: IllegalArgumentException) {
            throw InvalidArgumentException(e)
        }

        return null
    }

    private fun fromBase64String(s: String?): String {
        return String(Base64.decode(s, Base64.DEFAULT), StandardCharsets.UTF_8)
    }

    // Clip feature should run with main thread
    private inner class SetClipboardRunnable constructor(
            private val contentType: ClipboardDataType,
            private val label: String?,
            private val content: String
    ) : Runnable {

        override fun run() {
            when (contentType) {
                ClipboardDataType.PLAINTEXT -> ClipboardHelper(mInstrumentation.targetContext).setTextData(label, content)
            }
        }
    }
}
