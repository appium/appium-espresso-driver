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

package io.appium.espressoserver.lib.compose

import android.content.res.Resources
import androidx.test.platform.app.InstrumentationRegistry
import dalvik.system.DexFile
import io.appium.espressoserver.lib.helpers.AndroidLogger
import java.lang.reflect.Modifier

/**
 * The server APK comes first on the class path, so its generated Compose `R` classes shadow
 * the app's ones and hold IDs that do not exist in the app. This makes Compose fail to resolve
 * its own resources (e.g. the text field cursor handle popup title), which breaks recomposition.
 * Point these fields to the IDs of the app under test, matched by name.
 */
internal object ComposeResourceIds {
    private const val COMPOSE_PACKAGE_PREFIX = "androidx.compose."
    private val SKIPPED_TYPES = setOf("id", "attr", "styleable")

    @Suppress("DEPRECATION")
    fun alignWithTarget() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        var aligned = 0
        try {
            val dexFile = DexFile(instrumentation.context.packageCodePath)
            try {
                dexFile.entries().toList()
                    .filter { it.startsWith(COMPOSE_PACKAGE_PREFIX) && it.contains(".R$") }
                    .forEach { className ->
                        val type = className.substringAfter(".R$")
                        if (type !in SKIPPED_TYPES) {
                            aligned += alignClass(className, type, targetContext.resources, targetContext.packageName)
                        }
                    }
            } finally {
                dexFile.close()
            }
        } catch (e: Exception) {
            AndroidLogger.info("Could not align Compose resource IDs with the app: $e")
        }
        AndroidLogger.info("Aligned $aligned Compose resource IDs with the app under test")
    }

    private fun alignClass(
        className: String,
        type: String,
        resources: Resources,
        packageName: String,
    ): Int {
        var aligned = 0
        try {
            Class.forName(className).fields
                .filter { Modifier.isStatic(it.modifiers) && !Modifier.isFinal(it.modifiers) && it.type == Int::class.javaPrimitiveType }
                .forEach { field ->
                    val id = resources.getIdentifier(field.name, type, packageName)
                    if (id != 0 && id != field.getInt(null)) {
                        field.setInt(null, id)
                        aligned++
                    }
                }
        } catch (e: ReflectiveOperationException) {
            AndroidLogger.info("Could not align $className: $e")
        }
        return aligned
    }
}
