package io.appium.espressoserver.lib.model

import android.view.ViewConfiguration
import com.google.gson.annotations.SerializedName
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.*
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.*
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.POINTER
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH
import io.appium.espressoserver.lib.helpers.w3c.models.Origin
import java.util.*

fun toW3CInputSources(touchActionsLists: List<List<TouchAction>>): List<InputSource> {
    val inputSources = ArrayList<InputSource>()
    for ((touchInputIndex, touchActions) in touchActionsLists.withIndex()) {
        val w3cActions = ArrayList<Action>()
        for (touchAction in touchActions) {
            w3cActions.addAll(touchAction.toW3CAction())
        }

        val parameters = Parameters()
        parameters.pointerType = TOUCH

        // Add a finger pointer
        inputSources.add(InputSourceBuilder()
                .withType(POINTER)
                .withParameters(parameters)
                .withId(String.format("finger%s", touchInputIndex))
                .withActions(w3cActions)
                .build())
    }

    return inputSources
}

class TouchAction {

    private val TAP_TIMEOUT = ViewConfiguration.getTapTimeout().toLong()
    private val LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout().toLong()

    // Make the standard press duration be between TAP and LONG_PRESS timeouts
    private val PRESS_DURATION = (TAP_TIMEOUT + LONG_PRESS_TIMEOUT) / 2

    // Number of ms to add or subtract to a timeout so that it isn't the exact number
    private val TIMEOUT_BUFFER: Long = 10

    var action: ActionType? = null
    var options: TouchActionOptions? = null

    private val origin: Origin
        get() {
            val origin = Origin()
            options?.elementId?.let {
                origin.type = Origin.ELEMENT
                origin.elementId = it
            } ?: run {
                origin.type = Origin.VIEWPORT
            }
            return origin
        }

    private val moveTo: Action
        get() = ActionBuilder()
                .withType(POINTER_MOVE)
                .withX(options!!.x)
                .withY(options!!.y)
                .withOrigin(origin)
                .build()

    private val pause: Action
        get() = ActionBuilder()
                .withType(PAUSE)
                .withDuration(0L)
                .build()

    @Throws(InvalidArgumentException::class)
    fun toW3CAction(): List<Action> {
        val w3cActions: List<Action> = when (action) {
            ActionType.MOVE_TO -> listOf(convertMoveTo())
            ActionType.PRESS -> convertPress(PRESS_DURATION)
            ActionType.LONG_PRESS -> convertPress(LONG_PRESS_TIMEOUT + TIMEOUT_BUFFER)
            ActionType.TAP -> convertPress(TAP_TIMEOUT - TIMEOUT_BUFFER)
            ActionType.RELEASE -> listOf(convertRelease())
            ActionType.WAIT -> listOf(convertWait())
            ActionType.CANCEL -> listOf(convertCancel())
            else -> throw InvalidArgumentException(String.format("Unsupported action type %s", action))
        }

        // All touch actions map to 3 actions
        // For multi-touch actions we need each event to happen synchronously with eachother

        // e.g.) if one input calls press (which maps to move + down + wait) and another input is
        // calling pause (which maps to wait) we need to add two no-ops to the wait event so that it
        // doesn't prematurely advance to the next action before the 'press' event finishes
        return padActionsList(w3cActions)
    }

    private fun convertCancel(): Action {
        return ActionBuilder()
                .withType(POINTER_CANCEL)
                .build()
    }

    private fun convertWait(): Action {
        return ActionBuilder()
                .withType(PAUSE)
                .build()
    }

    private fun convertRelease(): Action {
        return ActionBuilder()
                .withType(POINTER_UP)
                .build()
    }

    private fun convertMoveTo(): Action {
        return moveTo
    }

    private fun convertPress(pressDuration: Long?): List<Action> {
        // Move to spot
        val moveAction = moveTo

        // Press down
        val downAction = ActionBuilder()
                .withType(POINTER_DOWN)
                .build()

        // Wait for the press duration
        val waitAction = ActionBuilder()
                .withType(PAUSE)
                .withDuration(pressDuration)
                .build()

        return listOf(moveAction, downAction, waitAction)
    }

    // If an action list has fewer than three actions, pad them with 'pauses' of 0 duration
    private fun padActionsList(actions: List<Action>): List<Action> {
        // One Jsonwp Touch Action maps to three W3C Actions
        val W3C_ACTIONS_PER_TOUCH_ACTION = 3

        val paddedActions = ArrayList<Action>(3)
        for (padIndex in 0 until W3C_ACTIONS_PER_TOUCH_ACTION - actions.size) {
            paddedActions.add(pause)
        }
        for (actionIndex in actions.indices) {
            paddedActions.add(actions[actionIndex])
        }

        return paddedActions
    }

    enum class ActionType {
        @SerializedName("moveTo")
        MOVE_TO,
        @SerializedName("tap")
        TAP,
        @SerializedName("press")
        PRESS,
        @SerializedName("longPress")
        LONG_PRESS,
        @SerializedName("release")
        RELEASE,
        @SerializedName("wait")
        WAIT,
        @SerializedName("cancel")
        CANCEL
    }

    class TouchActionOptions {
        @SerializedName("element", alternate = [W3C_ELEMENT_KEY])
        var elementId: String? = null
        var x: Long? = null
        var y: Long? = null
    }
}
