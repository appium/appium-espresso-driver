package io.appium.espressoserver.lib.handlers;

import android.app.Activity;
import android.util.ArrayMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.InvocationOperation;
import io.appium.espressoserver.lib.model.MobileBackdoorMethod;
import io.appium.espressoserver.lib.model.MobileBackdoorParams;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

public class MobileBackdoor implements RequestHandler<MobileBackdoorParams, String> {

    @Override
    public String handle(final MobileBackdoorParams params) throws AppiumException {
        logger.info("Invoking Backdoor");
        Activity activity = getCurrentActivity();

        List<InvocationOperation> ops = getBackdoorOperations(params);

        if (ops.isEmpty()) {
            throw new InvalidArgumentException("Please pass name(s) of method to be invoked");
        }

//        First try to find the method in Application object
        Object invocationResult = safeInvokeOnApplication(activity, ops);

        // if backdoor method not found in Application, try to find the method in urrent Activity object
        if (invocationResult instanceof Map && ((Map) invocationResult).containsKey("error")) {
            invocationResult = invokeOnActivity(activity, ops);
        }

        return invocationResult.toString();
    }

    private Object invokeOnActivity(Activity activity, List<InvocationOperation> ops) throws AppiumException {
        Object invocationResult = null;
        Object invokeOn = activity;
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

    private Object safeInvokeOnApplication(Activity activity, List<InvocationOperation> ops) {
        Object invocationResult = null;
        Object invokeOn = activity.getApplication();
        for (InvocationOperation op : ops) {
            try {
                invocationResult = op.apply(invokeOn);
                invokeOn = invocationResult;
            } catch (Exception e) {
                e.printStackTrace();
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


    //    https://androidreclib.wordpress.com/2014/11/22/getting-the-current-activity/
    private Activity getCurrentActivity() throws AppiumException {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            ArrayMap activities = (ArrayMap) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (Exception e) {
            throw new AppiumException(e);
        }
        return null;
    }
}
