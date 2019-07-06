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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import java.util.TimeZone
import java.util.Locale

class DeviceInfoHelper(private val context: Context) {

    private val defaultDisplay: Display?
        get() {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE)
            return (windowManager as? WindowManager)?.defaultDisplay;
        }

    /**
     * A unique serial number identifying a device, if a device has multiple users,  each user appears as a
     * completely separate device, so the ANDROID_ID value is unique to each user.
     * See https://developer.android.com/reference/android/provider/Settings.Secure.html#ANDROID_ID
     * for more info.
     *
     * @return ANDROID_ID A 64-bit number (as a hex string) that is uniquely generated when the user
     * first sets up the device and should remain constant for the lifetime of the user's device. The value
     * may change if a factory reset is performed on the device.
     */
    val androidId: String
        @SuppressLint("HardwareIds")
        get() = Secure.getString(context.contentResolver, Secure.ANDROID_ID)

    /**
     * @return Build.MANUFACTURER value
     */
    val manufacturer: String
        get() = Build.MANUFACTURER

    /**
     * @return Build.MODEL value
     */
    val modelName: String
        get() = Build.MODEL

    /**
     * @return Build.BRAND value
     */
    val brand: String
        get() = Build.BRAND

    /**
     * Current running OS's API VERSION
     *
     * @return the os version as String
     */
    val apiVersion: String
        get() = Integer.toString(Build.VERSION.SDK_INT)

    /**
     * @return The current version string, for example "1.0" or "3.4b5"
     */
    val platformVersion: String
        get() = Build.VERSION.RELEASE

    /**
     * @return The logical density of the display in Density Independent Pixel units.
     */
    val displayDensity: Int?
        get() {
            val display = defaultDisplay ?: return null
            val metrics = DisplayMetrics()
            display.getRealMetrics(metrics)
            return (metrics.density * 160).toInt()
        }

    /**
     * Retrievs the name of the current celluar network carrier
     *
     * @return carrier name or null if it cannot be retrieved
     */
    val carrierName: String?
        get() {
            val telephonyManager = context
                    .getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            try {
                return telephonyManager?.networkOperatorName
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

    /**
     * Retrieves the real size of the default display
     *
     * @return The display size in 'WxH' format
     */
    val realDisplaySize: String?
        get() {
            val display = defaultDisplay ?: return null
            val p = android.graphics.Point()
            display.getRealSize(p)
            return "${p.x}x${p.y}"
        }

    /**
     * Get current system locale
     *
     * @return The locale as string
     */
    val locale: String
        get() = Locale.getDefault().toString()

    /**
     * Get current system timezone
     * e.g. "Asia/Tokyo", "America/Caracas"
     *
     * @return The timezone as string
     */
    val timeZone: String
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TimeZone.getDefault().toZoneId().id
            } else {
                TimeZone.getDefault().id
            }
        }
}
