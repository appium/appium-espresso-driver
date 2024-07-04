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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class ClipboardHelper(private val context: Context) {
    private val DEFAULT_LABEL_LEN = 10

    private val manager: ClipboardManager
        get() = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val textData: String
        get() {
            val cm = manager
            if (!cm.hasPrimaryClip()) {
                return ""
            }
            val cd = cm.primaryClip
            if (cd == null || cd.itemCount == 0) {
                return ""
            }
            val text = cd.getItemAt(0).coerceToText(context)
            return text?.toString() ?: ""
        }

    fun setTextData(label: String?, data: String) {
        val cm = manager
        val labelToSet = label ?: if (data.length >= DEFAULT_LABEL_LEN)
            data.substring(0, DEFAULT_LABEL_LEN)
        else
            data
        cm.setPrimaryClip(ClipData.newPlainText(labelToSet, data))
    }
}
