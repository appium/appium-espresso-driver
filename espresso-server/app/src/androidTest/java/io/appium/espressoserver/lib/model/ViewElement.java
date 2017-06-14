package io.appium.espressoserver.lib.model;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import javax.annotation.Nullable;

public class ViewElement {

    private final int id;
    private final boolean clickable;
    private final boolean longClickable;
    private final boolean focused;
    private final String className;
    private int index;
    private CharSequence contentDescription = "";
    private CharSequence text = null;
    private final Rect bounds;

    public ViewElement(View view) {
        // Get content description
        if (view.getContentDescription() != null)
            this.contentDescription = view.getContentDescription();

        // Get bounds
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        this.bounds = new Rect(l[0], l[1], l[0] + view.getWidth(), l[1] + view.getHeight());

        // Get ID
        id = view.getId(); // TODO: Not sure if we need this. Will leave for now

        // Get className
        className = view.getClass().getName();

        // Get the index
        ViewParent parent = view.getParent();
        try {
            for (int index = 0; index < ((ViewGroup) parent).getChildCount(); ++index) {
                View childView = ((ViewGroup) parent).getChildAt(index);
                if (childView.equals(view)) {
                    this.index = index;
                }
            }
        } catch (ClassCastException e) {
            // If it couldn't be cast to a ViewGroup, the parent has no children
        }

        // Get text (if applicable)
        if (view instanceof TextView) {
            text = ((TextView) view).getText();
        }

        // Get booleans
        clickable = view.isClickable();
        longClickable = view.isLongClickable();
        focused = view.isAccessibilityFocused();

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

    public CharSequence getContentDescription() {
        return contentDescription;
    }

    public Rect getBounds() {
        return bounds;
    }

    public String getId() {
        return id + "";
    }

    public boolean isClickable() {
        return clickable;
    }

    public boolean isLongClickable() {
        return longClickable;
    }

    public boolean isFocused() {
        return focused;
    }

    public String getClassName() {
        return className;
    }

    public int getIndex() {
        return index;
    }

    @Nullable
    public CharSequence getText() {
        return text;
    }
}
