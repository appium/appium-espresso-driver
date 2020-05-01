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

import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import java.util.regex.Pattern

class RegexpTextMatcher constructor(private val pattern: Pattern)
    : BoundedMatcher<View?, TextView>(TextView::class.java) {
    override fun matchesSafely(item: TextView): Boolean {
        val text = item.text ?: return false
        return pattern.matcher(text).find()
    }

    override fun describeTo(description: Description) {
        description.appendText("with regexp: ").appendValue(pattern.toString())
    }
}

fun withRegexp(pattern: Pattern): RegexpTextMatcher {
    return RegexpTextMatcher(pattern)
}
