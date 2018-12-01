package io.appium.espressoserver.test.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.appium.espressoserver.lib.model.BackdoorMethodArg;
import io.appium.espressoserver.lib.model.MobileBackdoorMethod;

import static junit.framework.Assert.fail;
import static org.junit.Assert.*;

public class MobileBackdoorMethodTest {

    @Test
    public void shouldParseArgumentsAndTypes() {
        MobileBackdoorMethod method = new MobileBackdoorMethod();
        List<BackdoorMethodArg> args = new ArrayList<>();
        BackdoorMethodArg arg1 = new BackdoorMethodArg();
        arg1.setType("java.lang.String");
        arg1.setValue("Oh");
        args.add(arg1);

        BackdoorMethodArg arg2 = new BackdoorMethodArg();
        arg2.setType("java.lang.Integer");
        arg2.setValue("10");
        args.add(arg2);

        BackdoorMethodArg arg3 = new BackdoorMethodArg();
        arg3.setType("int");
        arg3.setValue("20");
        args.add(arg3);

        BackdoorMethodArg arg4 = new BackdoorMethodArg();
        arg4.setType("Boolean");
        arg4.setValue("true");
        args.add(arg4);

        method.setArgs(args);
        assertArrayEquals(new Class[]{String.class, Integer.class, int.class, Boolean.class}, method.getArgumentTypes());
        assertArrayEquals(new Object[]{"Oh", 10, 20, true}, method.getArguments());

    }

    @Test
    public void shouldNotAllowInvalidArgumentTypes() {
        MobileBackdoorMethod method = new MobileBackdoorMethod();
        List<BackdoorMethodArg> args = new ArrayList<>();
        BackdoorMethodArg arg1 = new BackdoorMethodArg();
        arg1.setType("java.lang.Lol");
        arg1.setValue("Oh");
        args.add(arg1);
        method.setArgs(args);
        try {
            method.getArgumentTypes();
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains(
                    String.format("Class not found: java.lang.Lol")));
        }

    }

    @Test
    public void shouldNotAllowIncompatibleValueForGivenType() {
        MobileBackdoorMethod method = new MobileBackdoorMethod();
        List<BackdoorMethodArg> args = new ArrayList<>();
        BackdoorMethodArg arg1 = new BackdoorMethodArg();
        arg1.setType("int");
        arg1.setValue("lol");
        args.add(arg1);
        method.setArgs(args);
        try {
            method.getArguments();
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("For input string: \"lol\""));
        }

    }
}