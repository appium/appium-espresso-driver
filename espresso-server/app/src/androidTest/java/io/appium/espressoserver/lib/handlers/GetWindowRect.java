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

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.WindowRect;
import io.appium.espressoserver.lib.model.WindowSize;

public class GetWindowRect implements RequestHandler<AppiumParams, WindowSize> {

    @Override
    public WindowSize handle(AppiumParams params) throws AppiumException {
        Logger.info("Get window rect of the device");

        final WindowSize windowSize = new GetWindowSize().handle(params);

        final WindowRect windowRect = new WindowRect();
        windowRect.setHeight(windowSize.getHeight());
        windowRect.setWidth(windowSize.getWidth());

        return windowRect;
    }
}
