package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

enum class ClipboardDataType {
    @SerializedName("plaintext", alternate = ["PLAINTEXT"])
    PLAINTEXT;

    companion object {
        fun supportedDataTypes(): List<String> = values().map { it.toString().toLowerCase() }

        fun invalidClipboardDataType(contentType: ClipboardDataType?) : InvalidArgumentException {
            return InvalidArgumentException(
                "Only ${supportedDataTypes()} content types are supported. '$contentType' is given instead"
            )
        }
    }
}
