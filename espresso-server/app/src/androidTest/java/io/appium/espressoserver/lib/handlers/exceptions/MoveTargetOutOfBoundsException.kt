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

package io.appium.espressoserver.lib.handlers.exceptions

import fi.iki.elonen.NanoHTTPD
import io.appium.espressoserver.lib.helpers.Rect

class MoveTargetOutOfBoundsException : AppiumException {
    constructor() : super("Target provided for a move action is out of bounds")

    constructor(targetX: Float, targetY: Float, boundingRect: Rect) : super(String.format(
            "The target [%s, %s] for pointer interaction is not in the viewport %s and cannot be brought into the viewport",
            targetX, targetY, boundingRect.toShortString()
    )) {}

    constructor(message: String) : super(message) {}

    override fun error(): String {
        return "move target out of bounds"
    }

    override fun status(): NanoHTTPD.Response.Status {
        return NanoHTTPD.Response.Status.INTERNAL_ERROR
    }
}
