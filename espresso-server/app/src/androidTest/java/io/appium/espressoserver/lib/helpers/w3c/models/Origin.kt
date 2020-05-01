package io.appium.espressoserver.lib.helpers.w3c.models

import com.google.gson.annotations.JsonAdapter

@JsonAdapter(OriginDeserializer::class)
class Origin {
    var type: String? = InputSource.VIEWPORT
    var elementId: String? = null

    constructor() {}
    constructor(type: String) {
        this.type = type
    }
}