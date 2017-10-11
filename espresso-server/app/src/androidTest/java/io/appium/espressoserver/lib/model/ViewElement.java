package io.appium.espressoserver.lib.model;

import android.content.res.Resources;
import android.graphics.Rect;
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
        return view.isClickable();
    }

    public boolean isLongClickable() {
        return view.isLongClickable();
    }

    public boolean isCheckable() {
        return view instanceof Checkable;
    }

    public boolean isChecked() {
        return isCheckable() && ((Checkable) view).isChecked();
    }

    public boolean isFocused() {
        return isFocusable() && view.isAccessibilityFocused();
    }

    public boolean isVisible() {
        return view.getVisibility() == View.VISIBLE;
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
                }
            }
        }
        return "";
    }

    public String getClassName() {
        return view.getClass().getName();
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
        return view.isEnabled();
    }

    public boolean isFocusable() {
        return view.isFocusable();
    }

    public boolean isScrollable() {
        return view.isScrollContainer();
    }

    public boolean isPassword() {
        return (view instanceof TextView) &&
                isPasswordInputType(((TextView) view).getInputType());
    }

    public boolean isSelected() {
        return view.isSelected();
    }

    public int getRelativeLeft() {
        return view.getLeft();
    }

    public int getRelativeTop() {
        return view.getTop();
    }

    public String getPackageName() {
        // TBD
        return "";
    }
}