package io.appium.espressoserver.lib.helpers.w3c.state

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import java.util.*

/**
 * Active Input Source defined in W3C spec
 *
 * (see https://www.w3.org/TR/webdriver/#terminology-0)
 */
class ActiveInputSources {
    private val inputSources: MutableMap<String?, InputSource> = WeakHashMap()

    @Throws(InvalidArgumentException::class)
    fun addInputSource(inputSource: InputSource) {
        inputSources[inputSource.id] = inputSource
    }

    /**
     * Remove an input source and also remove it from InputStateTable
     * @param inputSource Source to remove
     */
    fun removeInputSource(inputSource: InputSource) {
        removeInputSource(inputSource.id)
    }

    fun removeInputSource(id: String?) {
        inputSources.remove(id)
    }

    fun getInputSource(inputSource: InputSource): InputSource? {
        return inputSources[inputSource.id]
    }

    fun getInputSource(id: String?): InputSource? {
        return inputSources[id]
    }

    fun hasInputSource(id: String?): Boolean {
        return inputSources.containsKey(id)
    }

    companion object {
        private val activeInputSources: MutableMap<String, ActiveInputSources> = WeakHashMap()

        /**
         * Get the `active input sources` table for a session
         *
         * @return Global instance of ActiveInputSources
         */
        @Synchronized
        fun getActiveInputSourcesForSession(sessionId: String): ActiveInputSources {
            var globalInputStateTable = activeInputSources[sessionId]
            if (globalInputStateTable == null) {
                activeInputSources[sessionId] = ActiveInputSources()
                globalInputStateTable = activeInputSources[sessionId]
            }
            return globalInputStateTable!!
        }
    }
}