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

import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.sugar.Web.WebInteraction
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.web.WebAtomsParams
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.AndroidLogger.logger
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.viewmatcher.WithView.withView
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

class WebAtoms : RequestHandler<WebAtomsParams, Void> {

    @Throws(AppiumException::class)
    override fun handle(webAtomsParams: WebAtomsParams): Void? {
        var webViewInteraction: WebInteraction<*>

        // TODO: Add a 'waitForDocument' feature

        // Initialize onWebView with web view matcher (if webviewEl provided)
        if (webAtomsParams.webviewElement != null) {
            logger.info("Initializing webView with webview el ${webAtomsParams.webviewElement}")
            val matcher = withView(Element.getViewById(webAtomsParams.webviewElement))
            webViewInteraction = onWebView(matcher)
        } else {
            logger.info("Initializing webView without selector")
            webViewInteraction = onWebView()
        }

        // Set forceJavascript enabled if provided
        if (webAtomsParams.forceJavascriptEnabled) {
            webViewInteraction.forceJavascriptEnabled()
        }

        // Iterate through methodsChain and call the atoms
        for (method in webAtomsParams.methodChain) {
            logger.info("Parsing web atom '${method.name}' with atom '${method.atom}'");
            for (interactionFunction:KFunction<*> in webViewInteraction.javaClass.kotlin.memberFunctions) {
                if (interactionFunction.name == method.name) {
                    val paramClassifier = interactionFunction.parameters.get(1).type.classifier;

                    // TODO: Make a parameter matcher
                    if (paramClassifier is KClass<*>) {
                        if (!(paramClassifier.simpleName == "Atom")) {
                            continue;
                        }
                    }
                    val atomClass = DriverAtoms::class
                    for (atomFunc in atomClass.functions) {
                        if (atomFunc.name == method.atom.name) {
                            val arr:Array<Any> = method.atom.args.toTypedArray()

                            // TODO: Make this more versatile. Should check expected parameters and provided parameters
                            // and if expected is 'enum' use enumValueOf
                            if (atomFunc.name == "findElement") {
                                val firstArr = arr[0]
                                if (firstArr is String) {
                                    arr[0] = Locator.valueOf(firstArr.toUpperCase())
                                    logger.info("Locator type is ${arr[0]}");
                                }
                                // TODO: Throw exception for bad Enum type
                            }
                            val atom = atomFunc.call(*arr)

                            try {
                                logger.info("Calling '${interactionFunction.name}(${atomFunc.name}(${arr.joinToString(", ")}))")
                                val res = interactionFunction.call(webViewInteraction, atom)

                                if (res is WebInteraction<*>) {
                                    webViewInteraction = res;
                                } else {
                                    // TODO: Throw an exception because interaction function returned wrong result
                                    // This block will probably never be reached
                                }
                            } catch (rte:ReflectiveOperationException) {
                                throw InvalidArgumentException("Could not execute web atom: '${method.atom}'. Reason: ${rte.cause}");
                            }

                        }
                    }
                    // TODO: Throw exception if atom function not found
                }
                // TODO: Throw exception if interaction function not found
            }
        }

        return null
    }
}
