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

package io.appium.espressoserver.lib.helpers

// The build relocates the bundled libraries under this package (see RelocateClassesTask)
const val SHADED_PACKAGE_PREFIX = "io.appium.espressoserver.shaded."

/**
 * Loads a class given by a client. Libraries bundled with the server may be relocated,
 * so the relocated name is tried first.
 */
fun forNameShaded(name: String): Class<*> =
    try {
        Class.forName(SHADED_PACKAGE_PREFIX + name)
    } catch (e: ClassNotFoundException) {
        Class.forName(name)
    }

/** Hides the relocation from the names that are shown to the user. */
fun String.stripShadedPrefix(): String = replace(SHADED_PACKAGE_PREFIX, "")
