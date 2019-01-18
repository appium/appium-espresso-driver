package io.appium.espressoserver.test.helpers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.KReflectionUtils
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class `KReflectionUtils Test` {

    @Test
    fun `"invokeMethod" should find methods specific to parameters provided and call it`(){
        val obj = TestClass()

        var methodResult = KReflectionUtils.invokeMethod(TestClass::class.java.kotlin, "plus", obj, 1, 2)
        assertTrue(methodResult is Number)
        assertEquals(methodResult, 3)

        methodResult = KReflectionUtils.invokeMethod(TestClass::class.java.kotlin, "plus", obj, 2)
        assertTrue(methodResult is Number)
        assertEquals(methodResult, 2)

        methodResult = KReflectionUtils.invokeMethod(TestClass::class.java.kotlin, "plus", obj, "A", 2)
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
        KReflectionUtils.invokeMethod(TestClass::class.java.kotlin, "plus", obj, "A", "B")
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