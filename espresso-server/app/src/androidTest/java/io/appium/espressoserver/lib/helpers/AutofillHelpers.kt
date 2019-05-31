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

import android.os.Build
import android.view.View
import android.view.autofill.AutofillManager

import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction

import org.hamcrest.Matcher
import org.hamcrest.Matchers

class DisableAutofillAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.any(View::class.java)
    }

    override fun getDescription(): String {
        return "Dismissing autofill picker"
    }

    override fun perform(uiController: UiController, view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.context.getSystemService(AutofillManager::class.java)?.cancel()
        }
    }
}
