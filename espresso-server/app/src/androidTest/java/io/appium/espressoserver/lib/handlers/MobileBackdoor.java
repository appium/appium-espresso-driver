package io.appium.espressoserver.lib.handlers;

import android.app.Activity;
import android.util.ArrayMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.InvocationOperation;
import io.appium.espressoserver.lib.http.response.AppiumResponse;
import io.appium.espressoserver.lib.model.AppiumStatus;
import io.appium.espressoserver.lib.model.MobileBackdoorMethod;
import io.appium.espressoserver.lib.model.MobileBackdoorParams;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

public class MobileBackdoor implements RequestHandler<MobileBackdoorParams, String> {

    @Override
    public String handle(final MobileBackdoorParams params) throws AppiumException {
        logger.info("Invoking Backdoor");
        Activity activity = getActivity();

        List<InvocationOperation> ops = getBackdoorOperations(params);

        Object invocationResult = null;

        Object InvokeOn = activity.getApplication();
        for (InvocationOperation op : ops) {
            try {
                invocationResult = op.apply(InvokeOn);
                InvokeOn = invocationResult;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (invocationResult instanceof Map && ((Map) invocationResult).containsKey("error")) {
            InvokeOn = activity;
            for (InvocationOperation op : ops) {
                try {
                    invocationResult = op.apply(InvokeOn);
                    InvokeOn = invocationResult;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return invocationResult.toString();
    }

    private List<InvocationOperation> getBackdoorOperations(MobileBackdoorParams params) {
        List<InvocationOperation> ops = new ArrayList<>();
        List<MobileBackdoorMethod> mobileBackdoorMethods = params.getOpts();

        for (MobileBackdoorMethod mobileBackdoorMethod : mobileBackdoorMethods) {
            String methodName = mobileBackdoorMethod.getName();
            List<Object> arguments = new ArrayList<Object>();
            if (mobileBackdoorMethod.getArgs() != null) {
                arguments = mobileBackdoorMethod.getArgs();
            }

            ops.add(new InvocationOperation(methodName, arguments));
        }
        return ops;
    }


    //    https://androidreclib.wordpress.com/2014/11/22/getting-the-current-activity/
    private Activity getActivity() {
        Activity activity = null;
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
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
                    activity = (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }
}
