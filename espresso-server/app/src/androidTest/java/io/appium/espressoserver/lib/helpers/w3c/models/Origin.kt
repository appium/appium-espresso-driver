package io.appium.espressoserver.lib.helpers.w3c.models

import com.google.gson.annotations.JsonAdapter

@JsonAdapter(OriginDeserializer::class)
class Origin {
    var type: String? = VIEWPORT
    var elementId: String? = null

    constructor()
    constructor(type: String) {
        this.type = type
    }
    constructor(type: String, elementId: String) {
        this.type = type
        this.elementId = elementId
    }

    companion object {
        const val VIEWPORT = "viewport"
        const val POINTER = "pointer"
        const val ELEMENT = "element-6066-11e4-a52e-4f735466cecf"
    }
}