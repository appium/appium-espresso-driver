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
package io.appium.espressoserver.lib.viewmatcher

import android.view.View
import io.appium.espressoserver.lib.helpers.extensions.withPermit
import io.appium.espressoserver.lib.model.SourceDocument
import io.appium.espressoserver.lib.model.AttributesEnum
import io.appium.espressoserver.lib.model.EspressoAttributes
import io.appium.espressoserver.lib.model.compileXpathExpression
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.util.concurrent.Semaphore

fun fetchIncludedAttributes(xpath: String): Set<AttributesEnum>? {
    if (xpath.contains("@*")) {
        return null
    }

    return EspressoAttributes().attributes.fold(mutableSetOf()) { acc, value ->
        if (xpath.contains("@$value")) {
            acc.add(value)
        }
        acc
    }
}

fun withXPath(root: View?, xpath: String, index: Int? = null): Matcher<View> {
    val expression = compileXpathExpression(xpath)
    val attributes = fetchIncludedAttributes(xpath)
    val matchedXPathViews = mutableListOf<View>()
    var didLookup = false
    val lookupGuard = Semaphore(1)
    return object : TypeSafeMatcher<View>() {
        override fun matchesSafely(item: View): Boolean {
            lookupGuard.withPermit {
                if (!didLookup) {
                    matchedXPathViews.addAll(
                        SourceDocument(root ?: item.rootView, attributes).findViewsByXPath(expression)
                    )
                    didLookup = true
                }
            }

            if (matchedXPathViews.isEmpty()) {
                return false
            }

            return if (index != null) {
                // If index is not null, match it with the xpath in the list at the provided index
                index < matchedXPathViews.size && matchedXPathViews[index] == item
            } else
                // If index is null, then we only check that the view is contained in the list of matched xpaths
                matchedXPathViews.contains(item)
        }

        override fun describeTo(description: Description) {
            description.appendText("looked for element with XPath $xpath, root: $root, index: $index")
        }
    }
}
