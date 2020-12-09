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

import io.appium.espressoserver.lib.model.ViewAttributesEnum
import io.appium.espressoserver.lib.viewmatcher.fetchIncludedAttributes
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class `Xpath Query Parser Tests` {

    @Test
    fun `should parse attributes from query`() {
        assertEquals(fetchIncludedAttributes("//*[@text=\"yolo\" and @enabled=\"true\"]"),
        setOf(ViewAttributesEnum.TEXT, ViewAttributesEnum.ENABLED))
    }

    @Test
    fun `should include all attributes for a wildcard`() {
        assertNull(fetchIncludedAttributes("//*[@*=\"yolo\"]"))
    }

    @Test
    fun `should include no attributes if the query has none`() {
        assertEquals(fetchIncludedAttributes("//*[@foo=\"yolo\"]"), setOf())
    }
}
