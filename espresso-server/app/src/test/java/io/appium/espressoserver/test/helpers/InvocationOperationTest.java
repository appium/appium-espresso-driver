package io.appium.espressoserver.test.helpers;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import io.appium.espressoserver.lib.helpers.InvocationOperation;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvocationOperationTest {

    private Executor mainThreadExecutor;
    private DummyBackdoorSubject invokeOn;

    @Before
    public void setUp() {
        mainThreadExecutor = new Executor() {
            @Override
            public void execute(@NonNull Runnable runnable) {
                runnable.run();
            }
        };
        invokeOn = new DummyBackdoorSubject();
    }

    @Test
    public void shouldReturnValidResults() throws Exception {
        InvocationOperation operation = new InvocationOperation("validMethod",
                new Object[]{}, new Class[0], mainThreadExecutor);
        Object result = operation.apply(invokeOn);
        assertEquals(DummyBackdoorSubject.RETURN_VALUE, result);
    }

    @Test
    public void shouldReturnVoidIfReturnTypeIsVoid() throws Exception {
        InvocationOperation operation = new InvocationOperation("voidReturn",
                new Object[0], new Class[0], mainThreadExecutor);
        Object result = operation.apply(invokeOn);
        assertEquals(InvocationOperation.VOID, result);
    }

    @Test
    public void shouldThrowExceptionIfMethodNotFound() {
        String nonExistent = "nonExistent";
        InvocationOperation operation = new InvocationOperation(nonExistent,
                new Object[0], new Class[0], mainThreadExecutor);
        try {
            operation.apply(invokeOn);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains(
                    String.format("No public method %s definded", nonExistent)));
        }

    }

    @Test
    public void shouldBeAbleToChainMethods() throws Exception {
        InvocationOperation operation1 = new InvocationOperation("geObjectWithValue",
                new Object[]{45}, new Class[]{int.class}, mainThreadExecutor);

        InvocationOperation operation2 = new InvocationOperation("getTestValue",
                new Object[0], new Class[0], mainThreadExecutor);

        Object result = operation2.apply(operation1.apply(invokeOn));

        assertEquals(45, result);

    }


    public static class DummyBackdoorSubject {
        public static final String RETURN_VALUE = "LOL";

        private int testValue;

        @SuppressWarnings("unused")
        public void voidReturn() {
            // to test void return type
        }

        @SuppressWarnings("unused")
        public Object validMethod() {
            return RETURN_VALUE;
        }


        @SuppressWarnings("unused")
        public Object geObjectWithValue(int testValue) {
            this.testValue = testValue;
            return this;
        }

        @SuppressWarnings("unused")
        public int getTestValue() {
            return testValue;
        }

    }
}