package io.appium.espressoserver.test.helpers

import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.webdriver.DriverAtoms
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.KReflectionUtils
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.reflect.full.memberFunctions
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        assertTrue(findElementAtom is Atom<*>)
    }

    @Test
    fun `should parse Hamcrest 'instanceOf' matcher with className`() {
        arrayOf("java.lang.String", "java.lang.String.class", "String", "String.class")
            .forEach {className ->
                val hamcrestMatcher = KReflectionUtils.invokeMethod(Matchers::class, "instanceOf", className)
                assertTrue(hamcrestMatcher is Matcher<*>)
                assertTrue(hamcrestMatcher.matches("Hello World"))
                assertFalse(hamcrestMatcher.matches(123))
            }
    }

    @Test
    fun `should extract declared properties from an instance`() {
        data class TestData(val appPackage:String, val appActivity:String)
        val sessionParams = TestData("appPackage", "appActivity")
        val extractedProps = KReflectionUtils.extractDeclaredProperties(sessionParams)
        assertEquals(extractedProps["appPackage"], "appPackage")
        assertEquals(extractedProps["appActivity"], "appActivity")
    }

    class TestClass {
        fun plus (numOne: Int, numTwo: Int):Number {
            return numOne + numTwo
        }

        fun plus (num: Int):Number {
            return num
        }

        fun plus (dumbEnum: DumbEnum, num: Int): String {
            return when (dumbEnum) {
                DumbEnum.A -> "A$num"
                DumbEnum.B -> "B$num"
                DumbEnum.C -> "C$num"
            }
        }
    }

    enum class DumbEnum {
        A, B, C
    }
}