package io.appium.espressoserver.lib.handlers;

import android.app.Activity;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.ActivityHelper;
import io.appium.espressoserver.lib.helpers.InvocationOperation;
import io.appium.espressoserver.lib.model.MobileBackdoorMethod;
import io.appium.espressoserver.lib.model.MobileBackdoorParams;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

public class MobileBackdoor implements RequestHandler<MobileBackdoorParams, String> {

    @Override
    public String handle(final MobileBackdoorParams params) throws AppiumException {
        logger.info("Invoking Backdoor");
        Activity activity = ActivityHelper.getCurrentActivity();

        List<InvocationOperation> ops = getBackdoorOperations(params);

        if (ops.isEmpty()) {
            throw new InvalidArgumentException("Please pass name(s) of methods to be invoked");
        }

        // First try to find the method in Application object
        Object invocationResult = null;
        try {
            invocationResult = invokeBackdoorMethods(activity.getApplication(), ops);
        } catch (AppiumException e) {
            e.printStackTrace();
        }

        // if backdoor method not found in Application, try to find the method in Current Activity object
        if (invocationResult == null) {
            invocationResult = invokeBackdoorMethods(activity, ops);
        }

        if (invocationResult == null) {
            throw new AppiumException("Could not get valid results from Backdoor. Check adb logs");
        }

        return invocationResult.toString();
    }

    @Nullable
    private Object invokeBackdoorMethods(Object invokeOn, List<InvocationOperation> ops) throws AppiumException {
        Object invocationResult = null;

        for (InvocationOperation op : ops) {
            try {
                invocationResult = op.apply(invokeOn);
                invokeOn = invocationResult;
            } catch (Exception e) {
                throw new AppiumException(e);
            }
        }
        return invocationResult;
    }

    private List<InvocationOperation> getBackdoorOperations(MobileBackdoorParams params) throws InvalidArgumentException {
        List<InvocationOperation> ops = new ArrayList<>();
        List<MobileBackdoorMethod> mobileBackdoorMethods = params.getOpts();

        for (MobileBackdoorMethod mobileBackdoorMethod : mobileBackdoorMethods) {
            String methodName = mobileBackdoorMethod.getName();
            if (methodName == null) {
                throw new InvalidArgumentException("'name' is a required parameter for backdoor method to be invoked.");
            }
            ops.add(new InvocationOperation(methodName, mobileBackdoorMethod.getArgs()));
        }
        return ops;
    }

}
