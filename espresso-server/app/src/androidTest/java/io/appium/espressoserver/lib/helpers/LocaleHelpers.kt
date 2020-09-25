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

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.LocaleList
import java.util.*


fun changeLocale(context: Context, locale: Locale) {
    val res = context.applicationContext.resources
    val config = res.configuration

    Locale.setDefault(locale)
    if (SDK_INT >= Build.VERSION_CODES.O) {
        Locale.setDefault(Locale.Category.DISPLAY, locale)
    }

    if (SDK_INT >= Build.VERSION_CODES.N) {
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
        context.createConfigurationContext(config)
    } else {
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
    }
}
