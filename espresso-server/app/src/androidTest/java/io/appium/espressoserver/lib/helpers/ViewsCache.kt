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

import android.util.LruCache
import android.view.View

const val MAX_CACHE_SIZE = 500

data class ViewState(val view: View, val initialContentDescription: CharSequence?)

object ViewsCache {
    private val cache = LruCache<String, ViewState>(MAX_CACHE_SIZE)

    fun put(id: String, view: View) {
        cache.put(id, ViewState(view, view.contentDescription))
    }

    fun get(id: String): ViewState? = cache.get(id)

    fun has(id: String): Boolean = cache.get(id) != null

    fun reset() {
        cache.evictAll()
    }
}
