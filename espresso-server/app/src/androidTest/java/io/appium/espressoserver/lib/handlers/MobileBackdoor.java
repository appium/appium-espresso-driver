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

public class MobileBackdoor implements RequestHandler<MobileBackdoorParams, Object> {

    @Override
    public Object handle(final MobileBackdoorParams params) throws AppiumException {
        logger.info("Invoking Backdoor");
        if (params.getTarget() == null) {
            throw new InvalidArgumentException("Target must not be empty and must be of type: 'activity', 'application'");
        }

        Activity activity = ActivityHelper.getCurrentActivity();
        List<InvocationOperation> ops = getBackdoorOperations(params);
        Object invocationResult;
        switch (params.getTarget()) {
            case ACTIVITY:
                invocationResult = invokeBackdoorMethods(activity, ops);
                break;
            case APPLICATION:
                invocationResult = invokeBackdoorMethods(activity.getApplication(), ops);
                break;
            default:
                throw new InvalidArgumentException(String.format("target cannot be %s", params.getTarget()));
        }

        return invocationResult;

    }

    @Nullable
    private Object invokeBackdoorMethods(Object invokeOn, List<InvocationOperation> ops) throws AppiumException {
        Object invocationResult = null;
        Object invocationTarget = invokeOn;
        for (InvocationOperation op : ops) {
            try {
                invocationResult = op.apply(invocationTarget);
                invocationTarget = invocationResult;
            } catch (Exception e) {
                throw new AppiumException(e);
            }
        }
        return invocationResult;
    }

    private List<InvocationOperation> getBackdoorOperations(MobileBackdoorParams params) throws InvalidArgumentException {
        List<InvocationOperation> ops = new ArrayList<>();
        List<MobileBackdoorMethod> mobileBackdoorMethods = params.getMethods();

        for (MobileBackdoorMethod mobileBackdoorMethod : mobileBackdoorMethods) {
            String methodName = mobileBackdoorMethod.getName();
            if (methodName == null) {
                throw new InvalidArgumentException("'name' is a required parameter for backdoor method to be invoked.");
            }
            ops.add(new InvocationOperation(methodName, mobileBackdoorMethod.getArguments(),
                    mobileBackdoorMethod.getArgumentTypes()));
        }
        return ops;
    }

}
