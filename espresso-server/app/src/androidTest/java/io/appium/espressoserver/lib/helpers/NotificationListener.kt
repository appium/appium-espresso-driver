/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.helpers

import android.app.UiAutomation.OnAccessibilityEventListener
import android.view.accessibility.AccessibilityEvent
import java.util.*
import java.util.concurrent.Semaphore

const val TOAST_CLEAR_TIMEOUT = 3500L

object NotificationListener : OnAccessibilityEventListener {
    private val uiAutomation = UiAutomationWrapper
    private var recentToastTimestamp = 0L
    @Suppress("ObjectPropertyName")
    private val _toastMessage = mutableListOf<CharSequence>()
    private var originalListener: OnAccessibilityEventListener? = null
    private val TOAST_MESSAGE_GUARD = Semaphore(1)

    @Volatile
    var isListening = false
        private set

    fun start() {
        if (isListening) {
            AndroidLogger.logger.debug("Toast notification listener is already started")
            return
        }
        AndroidLogger.logger.debug("Starting toast notification listener")
        originalListener = uiAutomation.onAccessibilityEventListener
        isListening = true
        AndroidLogger.logger.debug("Original listener: $originalListener")
        uiAutomation.onAccessibilityEventListener = this
    }

    fun stop() {
        if (!isListening) {
            AndroidLogger.logger.debug("Toast notification listener is already stopped")
            return
        }
        AndroidLogger.logger.debug("Stopping toast notification listener")
        isListening = false
        uiAutomation.onAccessibilityEventListener = originalListener
    }

    @Synchronized
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            AndroidLogger.logger.debug("Caught toast message: $event")
            val text = event.text
            if (text != null && text.isNotEmpty()) {
                toastMessage = text
            }
        }
        originalListener?.onAccessibilityEvent(event)
    }

    var toastMessage: List<CharSequence>
        get() {
            TOAST_MESSAGE_GUARD.acquireUninterruptibly()
            try {
                if (_toastMessage.isNotEmpty() && System.currentTimeMillis() - recentToastTimestamp > TOAST_CLEAR_TIMEOUT) {
                    AndroidLogger.logger.info("Clearing the outdated toast message: $_toastMessage")
                    _toastMessage.clear()
                }
                return Collections.unmodifiableList(_toastMessage)
            } finally {
                TOAST_MESSAGE_GUARD.release()
            }
        }
        private set(text) {
            TOAST_MESSAGE_GUARD.acquireUninterruptibly()
            try {
                _toastMessage.clear()
                _toastMessage.addAll(text)
            } finally {
                TOAST_MESSAGE_GUARD.release()
            }
            recentToastTimestamp = System.currentTimeMillis()
        }
}
