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

package io.appium.espressoserver.lib.helpers.css

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import kotlin.math.roundToInt

class Style(private val properties: Array<Property>) {
    override fun toString(): String = properties.joinToString(separator = "; ")
}

fun extractStyle(view: View):Style {
    val cssProps = mutableListOf<Property>()

    cssProps.add(Left(view.left))
    cssProps.add(Top(view.top))
    cssProps.add(Width(view.width))
    cssProps.add(Height(view.height))

    cssProps.add(Padding(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom))

    cssProps.add(Opacity(view.alpha))

    cssProps.add(ZIndex(view.z.roundToInt()))

    val context = getApplicationContext<Context>()
    cssProps.add(Color(context.getColor(view.id)))

    val background = view.background
    if (background is ColorDrawable) {
        cssProps.add(BackgroundColor(background.color))
    }

    if (view is TextView) {
        cssProps.add(Font(view.typeface))
        cssProps.add(Text(view.gravity))
    }

    return Style(cssProps.filter { it.toString().trim().isNotEmpty() }.toTypedArray())
}
