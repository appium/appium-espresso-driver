package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

enum class ClipboardDataType {
    PLAINTEXT;

    companion object {
        fun invalidClipboardDataType(contentType: String?) : InvalidArgumentException {
            return InvalidArgumentException(
                "Only case insensitive ${values().map { it.toString() }} content types are supported. " +
                        "'$contentType' is given instead"
            )
        }
    }
}
