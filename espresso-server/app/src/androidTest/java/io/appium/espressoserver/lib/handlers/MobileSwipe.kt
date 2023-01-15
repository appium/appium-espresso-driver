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

import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.swipeDown

import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.UiController
import androidx.test.espresso.action.GeneralSwipeAction
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.getNodeInteractionById
import io.appium.espressoserver.lib.model.EspressoElement
import io.appium.espressoserver.lib.model.MobileSwipeParams
import io.appium.espressoserver.lib.model.MobileSwipeParams.Direction.*
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable
import io.appium.espressoserver.lib.viewaction.ViewGetter

class MobileSwipe : RequestHandler<MobileSwipeParams, Void?> {

    override fun handleEspresso(params: MobileSwipeParams): Void? {
        // Get a reference to the view and call onData. This will automatically scroll to the view.
        val viewInteraction = EspressoElement.getViewInteractionById(params.elementId)

        if (params.direction != null) {
            AndroidLogger.info("Performing swipe action with direction '${params.direction}'")
            when (params.direction) {
                UP -> viewInteraction.perform(androidx.test.espresso.action.ViewActions.swipeUp())
                DOWN -> viewInteraction.perform(androidx.test.espresso.action.ViewActions.swipeDown())
                LEFT -> viewInteraction.perform(androidx.test.espresso.action.ViewActions.swipeLeft())
                RIGHT -> viewInteraction.perform(androidx.test.espresso.action.ViewActions.swipeRight())
                else -> throw InvalidArgumentException(
                    "Unknown swipe direction '${params.direction}'. " +
                            "Only the following values are supported: " +
                            values().joinToString(",") { x -> x.name.lowercase() }
                )
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
                    """.trimIndent()
                    )
                    swipeAction.perform(uiController, ViewGetter().getView(viewInteraction))
                    return null
                }
            }
            UiControllerPerformer(runnable).run()
        }

        return null
    }

    override fun handleCompose(params: MobileSwipeParams): Void? {
        val nodeInteractions = getNodeInteractionById(params.elementId)

        AndroidLogger.info("Performing swipe action with direction '${params.direction}'")
        when (params.direction) {
            UP -> nodeInteractions.performGesture { swipeUp() }
            DOWN -> nodeInteractions.performGesture { swipeDown() }
            LEFT -> nodeInteractions.performGesture { swipeLeft() }
            RIGHT -> nodeInteractions.performGesture { swipeRight() }
            else -> throw InvalidArgumentException(
                "Unknown swipe direction '${params.direction}'. " +
                        "Only the following values are supported: " +
                        values().joinToString(",") { x -> x.name.lowercase() }
            )
        }
        return null
    }
}
