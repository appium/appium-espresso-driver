package io.appium.espressoserver.test.helpers

import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import io.appium.espressoserver.lib.helpers.ReflectionUtils
import io.appium.espressoserver.lib.helpers.ReflectionUtils.invokeStaticMethod
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class `KReflectionUtils Test` {

    @Test
    fun `"invokeMethod" should find methods specific to parameters provided and call it`(){
        val obj = TestClass()

        var methodResult = ReflectionUtils.invokeInstanceMethod(obj, "plus", null, 1)
        assertTrue(methodResult is String)
        assertEquals(methodResult, "1")

        methodResult = ReflectionUtils.invokeInstanceMethod(obj, "plus", DumbEnum.A, 1)
        assertTrue(methodResult is String)
        assertEquals(methodResult, "A1")

        methodResult = ReflectionUtils.invokeInstanceMethod(obj, "plus", 1, 2)
        assertTrue(methodResult is Number)
        assertEquals(methodResult, 3)
    }

    @Test
    fun `"invokeInstanceMethod" should extract method of an object`() {
        val methodResult = ReflectionUtils.extractMethod(TestClass::class.java, "plus", Int::class.java, Int::class.java)
        assertNotNull(methodResult)
    }

    @Test
    fun `"invokeInstanceMethod" should invoke method on instance of an object`() {
        val obj = TestClass()
        val methodResult = ReflectionUtils.invokeInstanceMethod(obj, "plus", 2, 2)
        assertTrue(methodResult is Number)
        assertEquals(methodResult, 4)
    }

    @Test(expected = Exception::class)
    fun `should fail with AppiumException if wrong invocation`() {
        val obj = TestClass()
        invokeStaticMethod(TestClass::class.java, "plus", obj, "A", "B")
    }

    @Test
    fun `should parse Driver Atoms "findElement"`() {
        val findElementAtom = invokeStaticMethod(DriverAtoms::class.java,
                "findElement", Locator.ID, "some Identifier")
        assertTrue(findElementAtom is Atom<*>)
    }

    @Test
    fun `should parse Hamcrest 'instanceOf' matcher with className`() {
        arrayOf("java.lang.String")
            .forEach {className ->
                val hamcrestMatcher = invokeStaticMethod(Matchers::class.java,
                        "instanceOf", Class.forName(className))
                assertTrue(hamcrestMatcher is Matcher<*>)
                assertTrue(hamcrestMatcher.matches("Hello World"))
                assertFalse(hamcrestMatcher.matches(123))
            }
    }

    @Test
    fun `should extract declared properties from an instance`() {
        data class TestData(val appPackage:String, val appActivity:String)
        val sessionParams = TestData("appPackage", "appActivity")
        val extractedProps = ReflectionUtils.extractDeclaredProperties(sessionParams)
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

        fun plus (dumbEnum: DumbEnum?, num: Int): String {
            return when (dumbEnum) {
                DumbEnum.A -> "A$num"
                DumbEnum.B -> "B$num"
                DumbEnum.C -> "C$num"
                else -> num.toString()
            }
        }
    }

    enum class DumbEnum {
        A, B, C
    }
}