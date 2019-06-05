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

import androidx.test.espresso.EspressoException
import androidx.test.espresso.contrib.ViewPagerActions
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.ScrollToPageParams
import io.appium.espressoserver.lib.model.ScrollToPageParams.ScrollTo.*

class ScrollToPage : RequestHandler<ScrollToPageParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: ScrollToPageParams): Void? {
        val viewInteraction = Element.getViewInteractionById(params.elementId)
        try {
            val smoothScroll = params.smoothScroll
            params.scrollTo?.let {
                val scrollAction = when (it) {
                    FIRST -> ViewPagerActions.scrollToFirst(smoothScroll)
                    LAST -> ViewPagerActions.scrollToLast(smoothScroll)
                    LEFT -> ViewPagerActions.scrollLeft(smoothScroll)
                    RIGHT -> ViewPagerActions.scrollRight(smoothScroll)
                }
                viewInteraction.perform(scrollAction)
                return null
            }

            params.scrollToPage?.let {
                viewInteraction.perform(ViewPagerActions.scrollToPage(it, smoothScroll))
                return null
            }

            throw InvalidArgumentException("Could not complete scrollToPage action. Must provide either 'scrollTo' or 'scrollToPage'")
        } catch (e: ClassCastException) {
            throw AppiumException("Could not perform scroll to on element ${params.elementId}. Reason: ${e}")
        } catch (e: Exception) {
            if (e is EspressoException) {
                throw AppiumException("Could not scroll to page. Reason: ${e}", e)
            }
            throw e
        }
    }
}
