package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

import java.util.Arrays

enum class ClipboardDataType {
    @SerializedName("PLAINTEXT")
    PLAINTEXT;


    companion object {

        fun supportedDataTypes(): String {
            return Arrays.toString(values())
        }
    }
}
