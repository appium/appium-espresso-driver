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

package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class ScrollToPageParams extends AppiumParams {
    private ScrollTo scrollTo;
    private Integer scrollToPage;
    private Boolean smoothScroll;

    @Nullable
    public ScrollTo getScrollTo() {
        return scrollTo;
    }

    public void setScrollTo(ScrollTo scrollTo) {
        this.scrollTo = scrollTo;
    }

    @Nullable
    public Integer getScrollToPage() {
        return scrollToPage;
    }

    public void setScrollTo(Integer scrollToPage) {
        this.scrollToPage = scrollToPage;
    }

    public Boolean getSmoothScroll() {
        return smoothScroll == null ? false : smoothScroll;
    }

    public void setSmoothScroll(Boolean smoothScroll) {
        this.smoothScroll = smoothScroll;
    }


    public static enum ScrollTo {
        @SerializedName("first")
        FIRST,
        @SerializedName("last")
        LAST,
        @SerializedName("left")
        LEFT,
        @SerializedName("right")
        RIGHT
    }
}
