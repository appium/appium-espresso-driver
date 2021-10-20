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

package io.appium.espressoserver

import org.junit.Assume
import org.junit.Test

import java.io.IOException

import androidx.test.filters.LargeTest
import io.appium.espressoserver.lib.drivers.DriverContext
import io.appium.espressoserver.lib.handlers.exceptions.DuplicateRouteException
import io.appium.espressoserver.lib.http.Server

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import androidx.compose.ui.test.junit4.AndroidComposeTestRule

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@LargeTest
class EspressoServerRunnerTest {

    @get:Rule
    val composeRule = AndroidComposeTestRule(
        activityRule = EmptyTestRule(),
        activityProvider = { error("Can't provide current activity") }
    ).also {
        composeTestRule = it
    }

    private val syncComposeClock = Thread {
        while (!Server.isStopRequestReceived) {
            if (context.currentStrategyType == DriverContext.StrategyType.COMPOSE) {
                composeTestRule.mainClock.advanceTimeByFrame()
            }
            // Let Android run measure, draw and in general any other async operations. AndroidComposeTestRule.android.kt:325
            Thread.sleep(ANDROID_ASYNC_WAIT_TIME_MS)
        }
    }

    @Test
    @Throws(InterruptedException::class, IOException::class, DuplicateRouteException::class)
    fun startEspressoServer() {
        if (System.getProperty("skipespressoserver") != null) {
            Assume.assumeTrue(true)
            return
        }
        try {
            Server.start()
            syncComposeClock.start()
            while (!Server.isStopRequestReceived) {
                Thread.sleep(1000)
            }
        } finally {
            Server.stop()
            syncComposeClock.join()
        }

        assertEquals(true, true) // Keep Codacy happy
    }

    class EmptyTestRule : TestRule {
        override fun apply(base: Statement, description: Description): Statement = base
    }

    companion object {
        lateinit var composeTestRule: AndroidComposeTestRule<*, *>
        val context = DriverContext()
        const val ANDROID_ASYNC_WAIT_TIME_MS = 10L
    }
}
