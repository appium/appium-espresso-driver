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

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.regex.Pattern
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.handlers.exceptions.NoAlertOpenException

import io.appium.espressoserver.lib.helpers.InteractionHelper.getUiDevice

object AlertHelpers {
    private const val regularAlertButtonResIdPrefix = "android:id/button"
    private val regularAlertButtonResIdPattern = Pattern.compile("^$regularAlertButtonResIdPrefix\\d+$")
    private const val alertContentResId = "android:id/content"
    private val regularAlertTitleResIdPattern = Pattern.compile(".+:id/(alertTitle|custom)$")
    private val permissionAlertTitleResIdPattern = Pattern.compile(".+:id/permission_message$")
    private val permissionAlertButtonResIdPattern = Pattern.compile(".+:id/permission_\\w+_button$")
    private val alertElementsResIdPattern = Pattern.compile(".+:id/.+")

    private val alertType: AlertType
        @Throws(NoAlertOpenException::class)
        get() {
            getUiDevice().waitForIdle()

            if (getUiDevice().findObjects(By.res(regularAlertTitleResIdPattern)).isNotEmpty()) {
                AndroidLogger.debug("Regular alert has been detected")
                return AlertType.REGULAR
            }
            if (getUiDevice().findObjects(By.res(permissionAlertTitleResIdPattern)).isNotEmpty()) {
                AndroidLogger.debug("Permission alert has been detected")
                return AlertType.PERMISSION
            }

            throw NoAlertOpenException()
        }

    /**
     * @return The actual text of the on-screen dialog. An empty
     * string is going to be returned if the dialog contains no text.
     * @throws NoAlertOpenException if no dialog is present on the screen
     */
    val text: String
        @Throws(NoAlertOpenException::class)
        get() {
            val alertType = alertType

            val alertRoots = getUiDevice().findObjects(By.res(alertContentResId))
            if (alertRoots.isEmpty()) {
                AndroidLogger.warn("Alert content container is missing")
                throw NoAlertOpenException()
            }

            val alertElements = alertRoots[0].findObjects(By.res(alertElementsResIdPattern))
            AndroidLogger.debug("Detected ${alertElements.size} alert elements")
            val alertButtonsResIdPattern = if (alertType == AlertType.REGULAR)
                regularAlertButtonResIdPattern.toString()
            else
                permissionAlertButtonResIdPattern.toString()
            return alertElements.toTypedArray()
                    .filter { it.resourceName != null && !it.resourceName.matches(alertButtonsResIdPattern.toRegex()) }
                    .filter { (it.text ?: "").isNotEmpty() }
                    .joinToString(separator = "\n")
        }

    private fun buttonResIdByIdx(index: Int): String {
        return "$regularAlertButtonResIdPrefix$index"
    }

    private fun filterButtonByLabel(buttons: Collection<UiObject2>, label: String?): UiObject2? {
        return buttons.toTypedArray().firstOrNull { it.text == label }
    }

    private fun getRegularAlertButton(action: AlertAction, buttonLabel: String?): UiObject2? {
        val alertButtonsMapping = HashMap<String, UiObject2>()
        val buttonIndexes = ArrayList<Int>()
        for (button in getUiDevice().findObjects(By.res(regularAlertButtonResIdPattern))) {
            val resId = button.resourceName
            alertButtonsMapping[resId] = button
            buttonIndexes.add(Integer.parseInt(resId.substring(regularAlertButtonResIdPrefix.length)))
        }
        if (alertButtonsMapping.isEmpty()) {
            return null
        }
        AndroidLogger.debug("Found ${alertButtonsMapping.size} buttons on the alert")

        if (buttonLabel == null) {
            val minIdx = Collections.min(buttonIndexes)
            return if (action == AlertAction.ACCEPT)
                alertButtonsMapping[buttonResIdByIdx(minIdx)]
            else
                alertButtonsMapping[buttonResIdByIdx(if (alertButtonsMapping.size > 1) minIdx + 1 else minIdx)]
        }
        return filterButtonByLabel(alertButtonsMapping.values, buttonLabel)
    }

    private fun getPermissionAlertButton(action: AlertAction, buttonLabel: String?): UiObject2? {
        val buttons = getUiDevice().findObjects(By.res(permissionAlertButtonResIdPattern))
        if (buttons.isEmpty()) {
            return null
        }
        AndroidLogger.debug("Found ${buttons.size} buttons on the alert")

        if (buttonLabel == null) {
            if (action == AlertAction.ACCEPT) {
                return if (buttons.size > 1) buttons[1] else buttons[0]
            }
            if (action == AlertAction.DISMISS) {
                return buttons[0]
            }
        } else {
            return filterButtonByLabel(buttons, buttonLabel)
        }
        return null
    }

    /**
     * Accept or dismiss on-screen alert.
     *
     * @param action      either ACCEPT or DISMISS
     * @param buttonLabel if this parameter is set then the method
     * will look for the dialog button with this particular
     * text instead of the default one (usually it is the first button
     * for ACCEPT and the last one for DISMISS action)
     * @return the actual label of the clicked button
     * @throws NoAlertOpenException         if no dialog is present on the screen
     * @throws InvalidElementStateException if no matching button can be found
     */
    @Throws(InvalidElementStateException::class, NoAlertOpenException::class)
    fun handle(action: AlertAction, buttonLabel: String?): String {
        val alertType = alertType

        val dstButton = (if (alertType == AlertType.REGULAR)
            getRegularAlertButton(action, buttonLabel)
        else
            getPermissionAlertButton(action, buttonLabel))
                ?: throw InvalidElementStateException("The expected button cannot be detected on the alert")

        val actualLabel = dstButton.text
        AndroidLogger.info("Clicking alert button '$actualLabel' in order to ${action.name.toLowerCase()} it")
        dstButton.click()
        return actualLabel
    }

    enum class AlertAction {
        ACCEPT, DISMISS
    }

    enum class AlertType {
        REGULAR, PERMISSION
    }
}
