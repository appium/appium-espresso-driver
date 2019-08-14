package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

enum class ClipboardDataType {
    PLAINTEXT;

    companion object {
        fun getContentType(contentType: String?): ClipboardDataType {
            if (contentType == null) return PLAINTEXT

            return when (contentType.toUpperCase()) {
                PLAINTEXT.name ->
                    PLAINTEXT
                else ->
                    throw InvalidArgumentException(
                        "Only case insensitive ${values().map { it.toString() }} content types are supported. " +
                                "'$contentType' is given instead")
            }

        }
    }
}
