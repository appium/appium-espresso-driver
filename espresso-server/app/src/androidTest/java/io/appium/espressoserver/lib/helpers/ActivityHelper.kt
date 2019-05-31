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

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.util.ArrayMap

import androidx.test.platform.app.InstrumentationRegistry

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException

object ActivityHelper {
    //    https://androidreclib.wordpress.com/2014/11/22/getting-the-current-activity/
    val currentActivity: Activity
        @Throws(AppiumException::class)
        get() {
            try {
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
                val activitiesField = activityThreadClass.getDeclaredField("mActivities")
                activitiesField.isAccessible = true
                val activities = activitiesField.get(activityThread) as ArrayMap<*, *>
                for (activityRecord in activities.values) {
                    val activityRecordClass = activityRecord.javaClass
                    val pausedField = activityRecordClass.getDeclaredField("paused")
                    pausedField.isAccessible = true
                    if (!pausedField.getBoolean(activityRecord)) {
                        val activityField = activityRecordClass.getDeclaredField("activity")
                        activityField.isAccessible = true
                        return activityField.get(activityRecord) as Activity
                    }
                }
            } catch (e: Exception) {
                throw AppiumException(e)
            }

            throw AppiumException("Failed to get current Activity")
        }

    /**
     * https://android.googlesource.com/platform/frameworks/base/+/master/tools/aapt/Resource.cpp#755
     *
     * @param instrumentation instrumentation instance
     * @param pkg app package name
     * @param activity activity name shortcut to be qualified
     * @return The qualified activity name
     */
    private fun getFullyQualifiedActivityName(instrumentation: Instrumentation,
                                              pkg: String?, activity: String): String {
        val appPackage = pkg ?: instrumentation.targetContext.packageName
        val dotPos = activity.indexOf(".")
        return (if (dotPos > 0) activity else "$appPackage${(if (dotPos == 0) "" else ".")}$activity")
    }

    fun startActivity(pkg: String?, activity: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val fullyQualifiedAppActivity = getFullyQualifiedActivityName(instrumentation, pkg, activity)
        AndroidLogger.logger.info("Starting activity '$fullyQualifiedAppActivity'")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClassName(instrumentation.targetContext, fullyQualifiedAppActivity)
        instrumentation.startActivitySync(intent)
    }
}
