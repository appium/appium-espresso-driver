package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

enum class ClipboardDataType {
    PLAINTEXT;

    companion object {
        private fun supportedDataTypes(): List<String> = values().map { it.toString().toLowerCase() }

        fun invalidClipboardDataType(contentType: String?) : InvalidArgumentException {
            return InvalidArgumentException(
                "Only ${supportedDataTypes()} content types are supported. '$contentType' is given instead"
            )
        }
    }
}
