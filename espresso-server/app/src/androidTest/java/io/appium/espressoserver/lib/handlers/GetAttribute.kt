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

package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.EspressoServerRunnerTest.Companion.context
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.ViewAttributesEnum

class GetAttribute : RequestHandler<AppiumParams, String?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: AppiumParams): String? {
        val attributeName = params.getUriParameterValue("name")
        if (attributeName == null || attributeName.trim { it <= ' ' }.isEmpty()) {
            throw AppiumException("Attribute name cannot be null or empty")
        }

        // Map attributeName to ENUM attribute
        ViewAttributesEnum.values().find { attributeName.equals(it.toString(), ignoreCase = true) }
            ?.let { attributeType -> return context.driverStrategy.getAttribute(params.elementId!!, attributeType) }

        // If we made it this far, we found no matching attribute. Throw an exception
        val supportedAttributeNames = ViewAttributesEnum.values().map { it.toString() }
        throw AppiumException("Attribute name should be one of $supportedAttributeNames. " +
                "'$attributeName' is given instead")
    }
}
