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

package io.appium.espressoserver.lib.model

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.View.NO_ID
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Checkable
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers

class ViewElement(private val view: View) {
    private var activity: Activity? = null

    val contentDescription: CharSequence?
        get() = view.contentDescription

    val bounds: Rect
        get() {
            val l = IntArray(2)
            view.getLocationOnScreen(l)
            return Rect(l[0], l[1], l[0] + view.width, l[1] + view.height)
        }

    val isClickable: Boolean
        get() = ViewMatchers.isClickable().matches(view)

    val isLongClickable: Boolean
        get() = view.isLongClickable

    val isCheckable: Boolean
        get() = view is Checkable

    val isChecked: Boolean
        get() = ViewMatchers.isChecked().matches(view)

    val isFocused: Boolean
        get() = isFocusable && view.isAccessibilityFocused

    val isVisible: Boolean
        get() = ViewMatchers.isDisplayed().matches(view)

    val id: Int
        get() = view.id

    // ignore
    val resourceId: String?
        get() {
            val id = id
            if (id != NO_ID) {
                val r = view.resources
                if (id > 0 && r != null) {
                    try {
                        val pkgname: String
                        when (id and -0x1000000) {
                            0x7f000000 -> pkgname = "app"
                            0x01000000 -> pkgname = "android"
                            else -> pkgname = r.getResourcePackageName(id)
                        }
                        return String.format("%s:%s/%s", pkgname,
                                r.getResourceTypeName(id), r.getResourceEntryName(id))
                    } catch (e: Resources.NotFoundException) {
                    }

                }
            }
            return null
        }

    // we want the index of the inner class
    // if this isn't an inner class, just find the start of the
    // top level class name.
    val className: String
        get() {
            var nameCopy = view.javaClass.name
            nameCopy = nameCopy.replace("\\$[0-9]+".toRegex(), "\\$")
            val start = nameCopy.lastIndexOf('$')
            return if (start < 0) {
                nameCopy
            } else nameCopy.substring(0, start)
        }

    val index: Int
        get() {
            val parentView = view.parent
            if (parentView is ViewGroup) {
                for (index in 0 until parentView.childCount) {
                    if (view == parentView.getChildAt(index)) {
                        return index
                    }
                }
            }
            return 0
        }

    val text: ViewText?
        get() {
            if (view is ProgressBar) {
                return ViewText(view.progress)
            }
            if (view is NumberPicker) {
                return ViewText(view.value)
            }
            if (view is TextView) {
                val textValue = view.text
                val hintValue = view.hint
                return if ((textValue == null || textValue.toString().isEmpty())
                        && hintValue != null && !hintValue.toString().isEmpty()) {
                    ViewText(hintValue.toString(), true)
                } else ViewText(textValue.toString(), false)
            }

            return null
        }

    val isEnabled: Boolean
        get() = ViewMatchers.isEnabled().matches(view)

    val isFocusable: Boolean
        get() = ViewMatchers.isFocusable().matches(view)

    val isScrollable: Boolean
        get() = view.isScrollContainer

    val isPassword: Boolean
        get() = view is TextView && isPasswordInputType(view.inputType)

    val isSelected: Boolean
        get() = ViewMatchers.isSelected().matches(view)

    val relativeLeft: Int
        get() = view.left

    val relativeTop: Int
        get() = view.top

    val packageName: String
        get() = getApplicationContext<Context>().packageName

    val viewTag: String?
        get() {
            val tag = view.tag
            return tag?.toString()
        }

    @Synchronized
    fun extractActivity(): Activity? {
        this.activity?.let {
            return it
        }

        var result = getActivity(view.context)
        if (result == null && view is ViewGroup) {
            var i = 0
            while (i < view.childCount && result == null) {
                result = getActivity(view.getChildAt(i).context)
                ++i
            }
        }
        this.activity = result
        return result
    }

    private fun getActivity(ctx: Context): Activity? {
        var context = ctx
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    private fun isPasswordInputType(inputType: Int): Boolean {
        val variation = inputType and (EditorInfo.TYPE_MASK_CLASS or EditorInfo.TYPE_MASK_VARIATION)
        return (variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                || variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
                || variation == EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)
    }
}
