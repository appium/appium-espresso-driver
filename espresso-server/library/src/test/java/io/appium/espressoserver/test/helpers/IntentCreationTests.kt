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

package io.appium.espressoserver.test.helpers

import android.content.Intent
import io.appium.espressoserver.lib.helpers.makeIntent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.Exception
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class `Intent Creation Tests` {

    @Test(expected = Exception::class)
    fun `should throw if none of the required options have been provided`(){
        makeIntent(null, mapOf())
    }

    @Test(expected = Exception::class)
    fun `should throw if an invalid option name as been provided`(){
        makeIntent(null, mapOf(
                "foo" to "bar"
        ))
    }

    @Test
    fun `should create intent if options were set properly`(){
        assertEquals(makeIntent(null, mapOf(
                "action" to "io.appium.espresso"
        )).action, "io.appium.espresso")

        assertEquals(makeIntent(null, mapOf(
                "data" to "content://contacts/people/1"
        )).data!!.toString(), "content://contacts/people/1")

        assertEquals(makeIntent(null, mapOf(
                "type" to "image/png"
        )).type!!.toString(), "image/png")

        assertEquals(makeIntent(null, mapOf(
                "categories" to "android.intent.category.APP_CONTACTS, android.intent.category.DEFAULT"
        )).categories.size, 2)
    }

    @Test
    fun `should create intent with valid intFlags`(){
        assertEquals(makeIntent(null, mapOf(
                "action" to "io.appium.espresso",
                "intFlags" to "0x0F"
        )).flags, 0x0F)
    }

    @Test
    fun `should create intent with valid flags`(){
        assertEquals(makeIntent(null, mapOf(
                "action" to "io.appium.espresso",
                "flags" to "GRANT_READ_URI_PERMISSION, FLAG_EXCLUDE_STOPPED_PACKAGES"
        )).flags, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
    }

    @Test
    fun `should create intent with valid single extra args`(){
        val intent = makeIntent(null, mapOf(
                "action" to "io.appium.espresso",
                "e" to mapOf(
                        "bar" to "baz",
                        "bar5" to "baz5"
                ),
                "es" to mapOf("bar2" to "baz2"),
                "ei" to mapOf("int1" to 2L),
                "ez" to mapOf("bool1" to true),
                "el" to mapOf("long1" to 1L),
                "ef" to mapOf("float1" to 1.1)
        ))
        val extras = intent.extras!!
        assertEquals(extras.get("bar"), "baz")
        assertEquals(extras.get("bar5"), "baz5")
        assertEquals(extras.get("bar2"), "baz2")
        assertEquals(extras.getInt("int1"), 2)
        assertEquals(extras.getBoolean("bool1"), true)
        assertEquals(extras.getLong("long1"), 1L)
        assertTrue(extras.getFloat("float1") > 1.0)
    }

    @Test
    fun `should create intent with valid array extra args`(){
        val intent = makeIntent(null, mapOf<String, Any>(
                "action" to "io.appium.espresso",
                "eia" to mapOf("intarr" to "1,2, 3"),
                "ela" to mapOf("longarr" to "4, 5, 6"),
                "efa" to mapOf("floatarr" to "1.1, 2.2")
        ))
        val extras = intent.extras!!
        assertTrue(extras.getIntArray("intarr")!!.size == 3)
        assertTrue(extras.getLongArray("longarr")!!.size == 3)
        assertTrue(extras.getFloatArray("floatarr")!!.size == 2)
    }
}