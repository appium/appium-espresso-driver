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

import androidx.test.espresso.UiController
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.ViewActions.*
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.appium.espressoserver.lib.helpers.getSemanticsNode
import io.appium.espressoserver.lib.helpers.getNodeInteractionById
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.model.EspressoElement
import io.appium.espressoserver.lib.model.MobileSwipeParams
import io.appium.espressoserver.lib.model.MobileSwipeParams.Direction.*
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable

class MobileSwipe : RequestHandler<MobileSwipeParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleEspresso(params: MobileSwipeParams): Void? {
        // Get a reference to the view and call onData. This will automatically scroll to the view.
        val viewInteraction = EspressoElement.getViewInteractionById(params.elementId)

        if (params.direction != null) {
            AndroidLogger.info("Performing swipe action with direction '${params.direction}'")
            when (params.direction) {
                UP -> viewInteraction.perform(swipeUp())
                DOWN -> viewInteraction.perform(swipeDown())
                LEFT -> viewInteraction.perform(swipeLeft())
                RIGHT -> viewInteraction.perform(swipeRight())
                else -> throw InvalidArgumentException("Direction cannot be ${params.direction}")
            }
        } else if (params.swiper != null) {

            val runnable = object : UiControllerRunnable<Void?> {
                override fun run(uiController: UiController): Void? {
                    val swipeAction = GeneralSwipeAction(
                        params.swiper,
                        params.startCoordinates,
                        params.endCoordinates,
                        params.precisionDescriber
                    )
                    AndroidLogger.info("""
                    Performing general swipe action with parameters
                    swiper=[${params.swiper}] startCoordinates=[${params.startCoordinates}]
                    endCoordinates=[${params.endCoordinates}] precisionDescriber=[${params.precisionDescriber}]
                    """.trimIndent())
                    swipeAction.perform(uiController, EspressoElement.getViewById(params.elementId))
                    return null
                }
            }
            UiControllerPerformer(runnable).run()
        }

        return null
    }

    override fun handleCompose(params: MobileSwipeParams): Void? {
        // Get a reference to the compose node
        val nodeInteractions = getNodeInteractionById(params.elementId)

        require((param.direction !=null){"Must provide direction to swipe to :up,down,right,left"}
            AndroidLogger.info("Performing swipe action with direction '${params.direction}'")
            when (params.direction) {
                UP -> nodeInteractions.performTouchInput{swipeUp()}
                DOWN -> nodeInteractions.performTouchInput{swipeDown()}
                LEFT -> nodeInteractions.performTouchInput{swipeLeft()}
                RIGHT -> nodeInteractions.performTouchInput{swipeRight()}
                else -> throw InvalidArgumentException("Direction cannot be ${params.direction}")
            }
        return null
    }
}
