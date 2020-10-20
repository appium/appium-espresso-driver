package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

fun String.toClipboardDataType(): ClipboardDataType {
    return when (this.toUpperCase()) {
        ClipboardDataType.PLAINTEXT.name ->
            ClipboardDataType.PLAINTEXT
        else ->
            throw InvalidArgumentException(
                    "Only case insensitive ${ClipboardDataType.values().map { it.name }} content types are supported. " +
                            "'$this' is given instead")
    }
}

enum class ClipboardDataType {
    PLAINTEXT;
}
