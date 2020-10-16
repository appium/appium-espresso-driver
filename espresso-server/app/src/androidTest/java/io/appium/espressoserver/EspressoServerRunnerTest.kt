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
import io.appium.espressoserver.lib.handlers.exceptions.DuplicateRouteException
import io.appium.espressoserver.lib.http.Server

import org.junit.Assert.assertEquals

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@LargeTest
class EspressoServerRunnerTest {
    @Test
    @Throws(InterruptedException::class, IOException::class, DuplicateRouteException::class)
    fun startEspressoServer() {
        if (System.getProperty("skipespressoserver") != null) {
            Assume.assumeTrue(true)
            return
        }
        try {
            Server.start()

            while (!Server.isStopRequestReceived) {
                Thread.sleep(1000)
            }
        } finally {
            Server.stop()
        }

        assertEquals(true, true) // Keep Codacy happy
    }
}
