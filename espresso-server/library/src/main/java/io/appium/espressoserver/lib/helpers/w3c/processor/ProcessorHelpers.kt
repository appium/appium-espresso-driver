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
package io.appium.espressoserver.lib.helpers.w3c.processor

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

fun isNullOrPositive(num: Float?): Boolean {
    return num == null || num >= 0
}

@Throws(InvalidArgumentException::class)
fun throwArgException(index: Int, id: String?, message: String?) {
    throw InvalidArgumentException("action in actions[$index] of action input source with id '$id' $message")
}

@Throws(InvalidArgumentException::class)
fun assertNullOrPositive(index: Int, id: String?, propertyName: String?, propertyValue: Float?) {
    if (!isNullOrPositive(propertyValue)) {
        throwArgException(index, id,
                "must have property '$propertyName' be greater than or equal to 0 or undefined. Found $propertyValue")
    }
}