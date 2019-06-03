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

import androidx.test.espresso.web.sugar.Web.WebInteraction
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.KReflectionUtils.invokeInstanceMethod
import io.appium.espressoserver.lib.helpers.KReflectionUtils.invokeMethod
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.web.WebAtomsParams
import io.appium.espressoserver.lib.viewmatcher.WithView.withView

class WebAtoms : RequestHandler<WebAtomsParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: WebAtomsParams): Void? {
        var webViewInteraction:WebInteraction<*>

        // TODO: Add a 'waitForDocument' feature to wait for HTML DOCUMENT to be ready

        // Initialize onWebView with web view matcher (if webviewEl provided)
        params.webviewElement.let{ webviewElement ->
            AndroidLogger.logger.info("Initializing webView interaction on webview with el: '$webviewElement'")
            val matcher = withView(Element.getViewById(webviewElement))
            webViewInteraction = onWebView(matcher)
        }

        // Set forceJavascript enabled if provided
        if (params.forceJavascriptEnabled) {
            webViewInteraction.forceJavascriptEnabled()
        }

        // Iterate through methodsChain and call the atoms
        params.methodChain.forEach { method ->
            val atom = invokeMethod(DriverAtoms::class, method.atom.name, *method.atom.args)

            AndroidLogger.logger.info("Calling interaction '${method.name}' with the atom '${method.atom}'")

            val args: Array<Any?> = if (atom != null) arrayOf(atom) else emptyArray()
            val res = invokeInstanceMethod(webViewInteraction, method.name, *args) as? WebInteraction<*>
                    ?: throw InvalidArgumentException("'${method.name}' does not return a 'WebViewInteraction' object")

            webViewInteraction = res;
        }

        return null
    }
}
