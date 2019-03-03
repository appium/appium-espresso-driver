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

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.AppiumParams

import io.appium.espressoserver.lib.model.Session.getGlobalSession
import io.appium.espressoserver.lib.model.SessionParams
import java.util.Collections.unmodifiableList
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

class GetSessions : RequestHandler<AppiumParams, Collection<Map<String, Any?>>> {

    @Throws(AppiumException::class)
    override fun handle(params: AppiumParams): Collection<Map<String, Any?>> {
        getGlobalSession()?.let { session ->
            val caps = SessionParams.DesiredCapabilities::class.declaredMemberProperties
                    .filter { prop -> prop.isAccessible }
                    .fold(mutableMapOf<String, Any?>()) { acc, prop ->
                        acc[prop.name] = prop.get(session.desiredCapabilities)
                        acc
                    }.toMap()
            return listOf<Map<String, Any?>>(mapOf(
                    "id" to session.id,
                    "capabilities" to caps
            ))
        }
        return unmodifiableList(emptyList())
    }

}
