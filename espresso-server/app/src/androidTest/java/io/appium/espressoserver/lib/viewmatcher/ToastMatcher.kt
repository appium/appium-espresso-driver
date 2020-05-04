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
package io.appium.espressoserver.lib.viewmatcher

import android.view.WindowManager
import androidx.test.espresso.Root
import io.appium.espressoserver.lib.helpers.AndroidLogger
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    public override fun matchesSafely(root: Root): Boolean {

        val notToast = try {
            // 'TYPE_TOAST' is deprecated, so it will be removed in the future
            root.windowLayoutParams.get().type != WindowManager.LayoutParams::class.members.single { it.name == "TYPE_TOAST" }.call()
        } catch (e: NoSuchElementException) {
            AndroidLogger.logger.info("WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY " +
                    "was called instead of WindowManager.LayoutParams.TYPE_TOAST in this environment " +
                    "because the latter has been deprecated. This could affect toast elements lookup")
            root.windowLayoutParams.get().type != WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        return if (notToast) {
            false
        } else root.decorView.windowToken === root.decorView.applicationWindowToken
    }
}
