/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class Uiautomator implements RequestHandler<UiautomatorParams, List<Object>> {

    @Override
    public List<Object> handle(UiautomatorParams params) throws AppiumException {
        logger.info("Invoking Uiautomator2 Methods");

        if (null == params.getStrategy()) {
            throw new AppiumException(String.format("strategy should be one of %s",
                    UiautomatorParams.Strategy.Companion.getValidStrategyNames()));
        }

        if (null == params.getAction()) {
            throw new AppiumException(String.format("strategy should be one of %s",
                    UiautomatorParams.Action.Companion.getValidActionNames()));
        }

        String locator = params.getLocator();
        Integer index = params.getIndex();

        try {
            Method byMethod = ReflectionUtils.method(By.class, params.getStrategy().getNameOfStrategy(), String.class);
            BySelector bySelector = (BySelector) ReflectionUtils.invoke(byMethod, By.class, locator);
            Method uiObjectMethod = ReflectionUtils.method(UiObject2.class, params.getAction().getNameOfAction());
            List<UiObject2> uiObjects = getUiDevice().findObjects(bySelector);
            logger.info(String.format("Found %d UiObjects", uiObjects.size()));

            List<Object> result = new ArrayList<>();

            if (index == null) {
                for (UiObject2 uiObject2 : uiObjects) {
                    result.add(uiObjectMethod.invoke(uiObject2));
                }
                return result;
            }

            if (index >= uiObjects.size()) {
                throw new AppiumException(
                        String.format("Index %d is out of bounds for %d elements", index, uiObjects.size()));
            }

            result.add(uiObjectMethod.invoke(uiObjects.get(index)));
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AppiumException(e);
        }
    }
}
