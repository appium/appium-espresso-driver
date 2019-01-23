package io.appium.espressoserver.lib.model

import androidx.annotation.Nullable

class ViewText(@param:Nullable @get:Nullable
               val rawText: String?, isHint: Boolean) {
    var isHint = false

    val text: String
        get() = rawText ?: ""

    init {
        this.isHint = isHint
    }

    constructor(rawText: Int) : this(Integer.toString(rawText), false) {}

    override fun toString(): String {
        return text
    }
}
