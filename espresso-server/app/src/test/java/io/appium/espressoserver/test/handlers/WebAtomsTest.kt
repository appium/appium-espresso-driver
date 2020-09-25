package io.appium.espressoserver.test.handlers

import io.appium.espressoserver.lib.handlers.LOCATOR_ENUMS
import org.junit.Test
import kotlin.test.assertEquals

class WebAtomsTest {
    @Test
    fun shouldHasLocatorEnums() {
        assertEquals(LOCATOR_ENUMS, listOf(
            "CLASS_NAME",
            "CSS_SELECTOR",
            "ID",
            "LINK_TEXT",
            "NAME",
            "PARTIAL_LINK_TEXT",
            "TAG_NAME",
            "XPATH"
        ))
    }
}
