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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.Checkable;
import android.widget.TextView;

import javax.annotation.Nullable;

import static android.view.View.NO_ID;

public class ViewElement {
    private final View view;
    private Activity activity = null;

    public ViewElement(View view) {
        this.view = view;

        // TODO: Attributes that need to be added with examples
        // resource-id android:id/decor_content_parent
        // index 0
        // package	io.appium.android.apis
        // content-desc
        // checkable	false
        // checked	false
        // enabled	true
        // focusable	false
        // scrollable	false
        // password	false
        // selected	false
        // bounds	[0,0][1440,2560]
        // resource-id	android:id/decor_content_parent
        // instance	0
        // visibility

    }

    public synchronized Activity extractActivity() {
        if (this.activity == null) {
            Activity result = getActivity(view.getContext());
            if (result == null && (view instanceof ViewGroup)) {
                ViewGroup v = (ViewGroup) view;
                int c = v.getChildCount();
                for (int i = 0; i < c && result == null; ++i) {
                    result = getActivity(v.getChildAt(i).getContext());
                }
            }
            this.activity = result;
        }
        return this.activity;
    }

    @Nullable
    private static Activity getActivity(Context ctx) {
        Context context = ctx;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private static boolean isPasswordInputType(int inputType) {
        final int variation =
                inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);
    }

    @Nullable
    public CharSequence getContentDescription() {
        return view.getContentDescription();
    }

    public Rect getBounds() {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        return new Rect(l[0], l[1], l[0] + view.getWidth(), l[1] + view.getHeight());
    }

    public boolean isClickable() {
        return ViewMatchers.isClickable().matches(view);
    }

    public boolean isLongClickable() {
        return view.isLongClickable();
    }

    public boolean isCheckable() {
        return view instanceof Checkable;
    }

    public boolean isChecked() {
        return ViewMatchers.isChecked().matches(view);
    }

    public boolean isFocused() {
        return isFocusable() && view.isAccessibilityFocused();
    }

    public boolean isVisible() {
        return ViewMatchers.isDisplayed().matches(view);
    }

    public int getId() {
        return view.getId();
    }

    public String getResourceId() {
        final int id = getId();
        if (id != NO_ID) {
            final Resources r = view.getResources();
            if (id > 0 && r != null) {
                try {
                    String pkgname;
                    switch (id & 0xff000000) {
                        case 0x7f000000:
                            pkgname = "app";
                            break;
                        case 0x01000000:
                            pkgname = "android";
                            break;
                        default:
                            pkgname = r.getResourcePackageName(id);
                            break;
                    }
                    return String.format("%s:%s/%s", pkgname,
                            r.getResourceTypeName(id), r.getResourceEntryName(id));
                } catch (Resources.NotFoundException e) {
                    // ignore
                }
            }
        }
        return "";
    }

    public String getClassName() {
        String nameCopy = view.getClass().getName();
        nameCopy = nameCopy.replaceAll("\\$[0-9]+", "\\$");
        // we want the index of the inner class
        final int start = nameCopy.lastIndexOf('$');
        // if this isn't an inner class, just find the start of the
        // top level class name.
        if (start < 0) {
            return nameCopy;
        }
        return nameCopy.substring(0, start);
    }

    public int getIndex() {
        final ViewParent parent = view.getParent();
        try {
            for (int index = 0; index < ((ViewGroup) parent).getChildCount(); ++index) {
                View childView = ((ViewGroup) parent).getChildAt(index);
                if (childView.equals(view)) {
                    return index;
                }
            }
        } catch (ClassCastException e) {
            // If it couldn't be cast to a ViewGroup, the parent has no children
        }
        return 0;
    }

    @Nullable
    public CharSequence getText() {
        if (view instanceof TextView) {
            return ((TextView) view).getText();
        }
        return null;
    }

    public boolean isEnabled() {
        return ViewMatchers.isEnabled().matches(view);
    }

    public boolean isFocusable() {
        return ViewMatchers.isFocusable().matches(view);
    }

    public boolean isScrollable() {
        return view.isScrollContainer();
    }

    public boolean isPassword() {
        return (view instanceof TextView) &&
                isPasswordInputType(((TextView) view).getInputType());
    }

    public boolean isSelected() {
        return ViewMatchers.isSelected().matches(view);
    }

    public int getRelativeLeft() {
        return view.getLeft();
    }

    public int getRelativeTop() {
        return view.getTop();
    }

    public String getPackageName() {
        return InstrumentationRegistry.getTargetContext().getPackageName();
    }
}
