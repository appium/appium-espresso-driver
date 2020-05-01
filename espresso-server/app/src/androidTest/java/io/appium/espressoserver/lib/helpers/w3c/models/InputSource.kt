package io.appium.espressoserver.lib.helpers.w3c.models

import io.appium.espressoserver.lib.helpers.w3c.state.InputState
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState

const val VIEWPORT = "viewport"
const val POINTER = "pointer"
const val ELEMENT = "element-6066-11e4-a52e-4f735466cecf"

/**
 * InputSource
 *
 * (refer to https://www.w3.org/TR/webdriver/#terminology-0 of W3C spec)
 *
 * Represents a Virtual Device providing input events
 */
@Suppress("unused")
class InputSource {
    var type: InputSourceType? = null
    var id: String? = null
    var parameters: Parameters? = null
    var actions: List<Action>? = null
    private val state: InputState? = null

    constructor() {}
    constructor(type: InputSourceType?, id: String?, parameters: Parameters?, actions: List<Action>?) {
        this.type = type
        this.id = id
        this.parameters = parameters
        this.actions = actions
    }

    /**
     * Get the initial state of an Input Source
     * @return Get the initial input state (see 17.3 for info on Input State)
     */
    val defaultState: InputState?
        get() = when (type) {
            InputSourceType.POINTER -> PointerInputState(pointerType)
            InputSourceType.KEY -> KeyInputState()
            else -> null
        }

    // NOTE: The spec specifies that the default should be MOUSE. This is an exception
    // because the vast majority of use cases on a mobile device will use TOUCH.
    val pointerType: PointerType?
        get() {
            if (parameters != null) {
                return parameters!!.pointerType
            } else if (type == InputSourceType.POINTER) {
                // NOTE: The spec specifies that the default should be MOUSE. This is an exception
                // because the vast majority of use cases on a mobile device will use TOUCH.
                return PointerType.TOUCH
            }
            return null
        }

    enum class InputSourceType {
        POINTER, KEY, NONE
    }

    class Action {
        var type // type of action
                : ActionType? = null
        var duration // time in milliseconds
                : Float? = null
        var button // Button that is being pressed. Defaults to 0.
                : Int? = null
            get() { return field ?: 0 }
        var x // x coordinate of pointer
                : Float? = null
        var y // y coordinate of pointer
                : Float? = null
        var value // a string containing a single Unicode code point or a number
                : String? = null
        var origin = Origin() // origin; could be viewport, pointer or <{element-6066-11e4-a52e-4f735466cecf: <element-uuid>}>

        val isOriginViewport: Boolean
            get() = origin.type != null && origin.type.equals(VIEWPORT, ignoreCase = true)

        val isOriginPointer: Boolean
            get() = origin.type != null && origin.type.equals(POINTER, ignoreCase = true)

        val isOriginElement: Boolean
            get() = origin.type != null && origin.type.equals(ELEMENT_CODE, ignoreCase = true)

        companion object {
            // Web element identifier: https://www.w3.org/TR/webdriver/#elements
            // (note: in the Appium case it's not actually a "web" element, it's a native element)
            const val ELEMENT_CODE = "element-6066-11e4-a52e-4f735466cecf"
        }
    }

    enum class ActionType {
        PAUSE, POINTER_DOWN, POINTER_UP, POINTER_MOVE, POINTER_CANCEL, KEY_UP, KEY_DOWN
    }

    class Parameters {
        var pointerType: PointerType? = null
    }

    enum class PointerType {
        MOUSE, PEN, TOUCH
    }

    class InputSourceBuilder {
        private var type: InputSourceType? = null
        private var id: String? = null
        private var parameters: Parameters? = null
        private var actions: List<Action>? = null
        fun withType(type: InputSourceType?): InputSourceBuilder {
            this.type = type
            return this
        }

        fun withId(id: String?): InputSourceBuilder {
            this.id = id
            return this
        }

        fun withParameters(parameters: Parameters?): InputSourceBuilder {
            this.parameters = parameters
            return this
        }

        fun withActions(actions: List<Action>?): InputSourceBuilder {
            this.actions = actions
            return this
        }

        fun build(): InputSource {
            val inputSource = InputSource()
            inputSource.actions = actions
            inputSource.type = type
            inputSource.id = id
            inputSource.parameters = parameters
            return inputSource
        }
    }

    class ActionBuilder {
        private var type: ActionType? = null
        private var duration: Long? = null
        private var button: Int? = null
        private var x: Long? = null
        private var y: Long? = null
        private var value: String? = null
        private var origin = Origin()
        fun withType(type: ActionType?): ActionBuilder {
            this.type = type
            return this
        }

        fun withDuration(duration: Long?): ActionBuilder {
            this.duration = duration
            return this
        }

        fun withButton(button: Int?): ActionBuilder {
            this.button = button
            return this
        }

        fun withX(x: Long?): ActionBuilder {
            this.x = x
            return this
        }

        fun withY(y: Long?): ActionBuilder {
            this.y = y
            return this
        }

        fun withValue(value: String?): ActionBuilder {
            this.value = value
            return this
        }

        fun withOrigin(origin: Origin): ActionBuilder {
            this.origin = origin
            return this
        }

        fun withOrigin(originType: String?): ActionBuilder {
            origin.type = originType
            return this
        }

        fun withElementId(elementId: String?): ActionBuilder {
            origin.type = ELEMENT
            origin.elementId = elementId
            return this
        }

        fun build(): Action {
            val action = Action()
            duration?.let {
                action.duration = it.toFloat()
            }
            action.type = type
            button?.let {
                action.button = it
            }
            action.value = value
            x?.let {
                action.x = it.toFloat()
            }
            y?.let {
                action.y = it.toFloat()
            }
            action.origin = origin
            return action
        }
    }
}