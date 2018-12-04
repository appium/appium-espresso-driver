package io.appium.espressoserver.lib.handlers;

import android.app.Activity;

import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.ActivityHelper;
import io.appium.espressoserver.lib.helpers.BackdoorUtils;
import io.appium.espressoserver.lib.helpers.InvocationOperation;
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
        List<InvocationOperation> ops = BackdoorUtils.getOperations(params.getMethods());

        switch (params.getTarget()) {
            case ACTIVITY:
                return BackdoorUtils.invokeMethods(activity, ops);
            case APPLICATION:
                return BackdoorUtils.invokeMethods(activity.getApplication(), ops);
            default:
                throw new InvalidArgumentException(String.format("target cannot be %s", params.getTarget()));
        }

    }

}
