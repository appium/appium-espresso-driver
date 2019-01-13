package io.appium.espressoserver.lib.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiObject2;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.ReflectionUtils;
import io.appium.espressoserver.lib.model.UiautomatorParams;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;
import static io.appium.espressoserver.lib.helpers.InteractionHelper.getUiDevice;

public class Uiautomator implements RequestHandler<UiautomatorParams, Object> {

    @Override
    public Object handle(UiautomatorParams params) throws AppiumException {
        logger.info("Invoking Uiautomator Methods");

        ArrayList<Object> result = new ArrayList<>();
        String byMethodName = params.getBy();
        String value = params.getValue();
        String action = params.getAction();
        Integer index = params.getIndex();

        try {
            Method byMethod = ReflectionUtils.method(By.class, byMethodName, String.class);
            BySelector selector = (BySelector) ReflectionUtils.invoke(byMethod, By.class, value);
            List<UiObject2> uiObjects = getUiDevice().findObjects(selector);

            Method uiObjectMethod = ReflectionUtils.method(UiObject2.class, action);
            if (index == null) {
                for (UiObject2 uio : uiObjects) {
                    result.add(uiObjectMethod.invoke(uio));
                }
            } else {
                result.add(uiObjectMethod.invoke(uiObjects.get(index)));
            }

        } catch ( IllegalAccessException | InvocationTargetException e) {
            throw new AppiumException(e);
        }
        return result;
    }

}
