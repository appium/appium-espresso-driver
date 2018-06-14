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

package io.appium.espressoserver.lib.helpers.w3c.processor;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;

@SuppressWarnings("unused")
public class ProcessorHelpers {
    public static boolean isNullOrPositive(Long num) {
        return num == null || num >= 0;
    }

    public static void throwArgException(int index, String id, String message) throws InvalidArgumentException {
        throw new InvalidArgumentException(String.format("action in actions[%s] of action input source with id '%s' %s",
                index, id, message));
    }

    public static void assertNullOrPositive(int index, String id, String propertyName, Long propertyValue) throws InvalidArgumentException {
        if (!isNullOrPositive(propertyValue)) {
            throwArgException(index, id, String.format(
                    "must have property '%s' be greater than or equal to 0 or undefined. Found %s", propertyName, propertyValue)
            );
        }
    }
}
