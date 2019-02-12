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

import android.view.View

sealed class ViewState {
    class ATTACHED(val view: View, val initialContentDescription: CharSequence?) : ViewState()
    class DETACHED(val view: View, val initialContentDescription: CharSequence?) : ViewState()
}

object ViewsCache : View.OnAttachStateChangeListener {
    private val cache = HashMap<String, ViewState>()

    // TODO: Decide if we want to run cleanup of detached views after some time interval

    override fun onViewAttachedToWindow(v: View?) {
        synchronized(cache) {
            val entriesToAdd = cache.entries
                    .filter { entry -> entry.value is ViewState.DETACHED && (entry.value as ViewState.DETACHED).view == v }
            if (entriesToAdd.isEmpty()) {
                v?.removeOnAttachStateChangeListener(this)
            } else {
                entriesToAdd.forEach {
                    cache[it.key] = ViewState.ATTACHED((it.value as ViewState.DETACHED).view,
                            (it.value as ViewState.DETACHED).initialContentDescription)
                }
            }
        }
    }

    override fun onViewDetachedFromWindow(v: View?) {
        synchronized(cache) {
            val entriesToRemove = cache.entries
                    .filter { entry -> entry.value is ViewState.ATTACHED && (entry.value as ViewState.ATTACHED).view == v }
            if (entriesToRemove.isEmpty()) {
                v?.removeOnAttachStateChangeListener(this)
            } else {
                entriesToRemove.forEach {
                    cache[it.key] = ViewState.DETACHED((it.value as ViewState.ATTACHED).view,
                            (it.value as ViewState.ATTACHED).initialContentDescription)
                }
            }
        }
    }

    fun put(id: String, view: View) {
        synchronized(cache) {
            // Make sure we never add the listener twice to the same view
            view.removeOnAttachStateChangeListener(this)
            view.addOnAttachStateChangeListener(this)
            cache.put(id, ViewState.ATTACHED(view, view.contentDescription))
        }
    }

    fun get(id: String): ViewState? {
        synchronized(cache) {
            return cache[id]
        }
    }

    fun has(id: String): Boolean {
        synchronized(cache) {
            return cache.containsKey(id)
        }
    }

    fun getViewId(view: View): String? {
        synchronized(cache) {
            return cache.entries
                    .filter {
                        (it.value is ViewState.ATTACHED && (it.value as ViewState.ATTACHED).view == view) ||
                                (it.value is ViewState.DETACHED && (it.value as ViewState.DETACHED).view == view)
                    }.map { it.key }
                    .firstOrNull()
        }
    }
}
