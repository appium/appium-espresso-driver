package io.appium.espressoserver.test.helpers

import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.ElementReference
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.KReflectionUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.reflect.full.memberFunctions
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.Locator

@RunWith(RobolectricTestRunner::class)
class `KReflectionUtils Test` {

    @Test
    fun `"invokeMethod" should find methods specific to parameters provided and call it`(){
        val obj = TestClass()

        var methodResult = KReflectionUtils.invokeMethod(TestClass::class.memberFunctions, "plus", obj, 1, 2)
        assertTrue(methodResult is Number)
        assertEquals(methodResult, 3)

        methodResult = KReflectionUtils.invokeMethod(TestClass::class.memberFunctions, "plus", obj, 2)
        assertTrue(methodResult is Number)
        assertEquals(methodResult, 2)

        methodResult = KReflectionUtils.invokeMethod(TestClass::class.memberFunctions, "plus", obj, "A", 2)
        assertTrue(methodResult is String)
        assertEquals(methodResult, "A2")
    }

    @Test
    fun `"invokeInstanceMethod" should invoke method on instance of an object`() {
        val obj = TestClass()
        val methodResult = KReflectionUtils.invokeInstanceMethod(obj, "plus", 2, 2)
        assertTrue(methodResult is Number)
        assertEquals(methodResult, 4)
    }

    @Test(expected = AppiumException::class)
    fun `should fail with AppiumException if wrong invocation`() {
        val obj = TestClass()
        KReflectionUtils.invokeMethod(TestClass::class, "plus", obj, "A", "B")
    }

    @Test
    fun `should parse Driver Atoms "findElement"`() {
        val findElementAtom = KReflectionUtils.invokeMethod(DriverAtoms::class, "findElement", "ID", "some Identifier")
        assertTrue(findElementAtom is Atom<*>);
    }

    @Test
    fun `should parse WebInteraction methods`() {
        val webInteraction = onWebView()
        KReflectionUtils.invokeInstanceMethod(webInteraction, "withElement", findElement(Locator.ID, "someID"))

    }

    class TestClass {
        fun plus (numOne: Int, numTwo: Int):Number {
            return numOne + numTwo;
        }

        fun plus (num: Int):Number {
            return num
        }

        fun plus (dumbEnum: DumbEnum, num: Int): String {
            when (dumbEnum) {
                DumbEnum.A -> return "A" + num
                DumbEnum.B -> return "B" + num
                DumbEnum.C -> return "C" + num
            }
        }
    }

    enum class DumbEnum {
        A, B, C
    }
}