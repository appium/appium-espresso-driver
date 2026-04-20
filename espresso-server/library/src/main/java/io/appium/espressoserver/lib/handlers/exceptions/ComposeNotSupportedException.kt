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

/**
 * Thrown when this server was built with Compose omitted (e.g.
 * `appium:espressoBuildConfig->composeSupport=false`) but a Compose-only action was requested.
 */
class ComposeNotSupportedException :
    InvalidArgumentException(
        "Jetpack Compose is not available in this Espresso server build: it was assembled with " +
            "appium:espressoBuildConfig->composeSupport=false. " +
            "Set appium:espressoBuildConfig->composeSupport=true and rebuild the server to use Compose.",
    )
