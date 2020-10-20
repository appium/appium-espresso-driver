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
import android.content.Context
import android.os.Build
import android.util.ArrayMap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import io.appium.espressoserver.lib.helpers.ReflectionUtils.extractField
import io.appium.espressoserver.lib.model.StartActivityParams
import io.appium.espressoserver.lib.model.mapToLocaleParams

object ActivityHelpers {
    val currentActivity: Activity
        get() {
            val method1 = fun(): Activity? {
                // https://stackoverflow.com/questions/38737127/espresso-how-to-get-current-activity-to-test-fragments/58684943#58684943
                var result: Activity? = null
                InstrumentationRegistry.getInstrumentation()
                        .runOnMainSync {
                            run {
                                result = ActivityLifecycleMonitorRegistry.getInstance()
                                        .getActivitiesInStage(Stage.RESUMED)
                                        .elementAtOrNull(0)
                            }
                        }
                return result
            }
            val method2 = fun(): Activity? {
                //    https://androidreclib.wordpress.com/2014/11/22/getting-the-current-activity/
                try {
                    val activityThreadClass = Class.forName("android.app.ActivityThread")
                    val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
                    val activities = extractField(activityThreadClass, "mActivities", activityThread) as ArrayMap<*, *>
                    return activities.values
                            .map { Pair(extractField(it.javaClass, "paused", it) as Boolean, it) }
                            .filter { !it.first }
                            .map { extractField(it.second.javaClass, "activity", it.second) as Activity }
                            .firstOrNull()
                } catch (e: Exception) {
                    // ignore
                }
                return null
            }
            for (method in listOf(method1, method2)) {
                val result = method()
                if (result != null) {
                    return result
                }
            }

            throw IllegalStateException("Cannot retrieve the current activity")
        }

    /**
     * https://android.googlesource.com/platform/frameworks/base/+/master/tools/aapt/Resource.cpp#755
     *
     * @param context Target context instance
     * @param pkg app package name
     * @param activity activity name shortcut to be qualified
     * @return The qualified activity name
     */
    private fun getFullyQualifiedActivityName(context: Context, pkg: String?, activity: String): String {
        val appPackage = pkg ?: context.packageName
        val dotPos = activity.indexOf(".")
        return (if (dotPos > 0) activity else "$appPackage${(if (dotPos == 0) "" else ".")}$activity")
    }

    fun startActivity(params: StartActivityParams) {
        if (params.appActivity == null && params.optionalIntentArguments == null) {
            throw IllegalArgumentException("Either activity name or intent options must be set")
        }

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        params.locale?.let {
            changeLocale(instrumentation.targetContext.applicationContext, mapToLocaleParams(it).toLocale())
        }

        val intent = if (params.optionalIntentArguments == null) {
            val fullyQualifiedAppActivity = getFullyQualifiedActivityName(instrumentation.targetContext,
                    params.appPackage, params.appActivity!!)
            val defaultOptions = mapOf<String, Any>(
                    "action" to "ACTION_MAIN",
                    "flags" to "ACTIVITY_NEW_TASK",
                    "className" to fullyQualifiedAppActivity
            )
            AndroidLogger.info("Starting activity '$fullyQualifiedAppActivity' " +
                    "with default options: $defaultOptions")
            makeIntent(instrumentation.targetContext, defaultOptions)
        } else {
            AndroidLogger.info("Staring activity with custom options: ${params.optionalIntentArguments}")
            makeIntent(instrumentation.targetContext, params.optionalIntentArguments)
        }

        if (params.optionalActivityArguments == null) {
            instrumentation.startActivitySync(intent)
        } else {
            makeActivityOptions(params.optionalActivityArguments).let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    instrumentation.startActivitySync(intent, it.toBundle())
                } else {
                    instrumentation.targetContext.startActivity(intent, it.toBundle())
                }
            }
        }
    }

}
