package io.appium.espressoserver.lib.helpers

import com.google.gson.JsonObject
import com.google.gson.JsonParseException

object AssertHelpers {

    fun assertIsPrimitive(obj: JsonObject, propName: String) {
        obj.get(propName)?.let { if (it.isJsonPrimitive) return; }
        throw JsonParseException("'${propName}' property is required and must be a JSON primitive")
    }

    fun assertIsPresent(obj: JsonObject, propName: String) {
        obj.get(propName) ?: throw JsonParseException("'${propName}' is a required field");
    }

}