package io.appium.espressoserver.lib.helpers;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.view.ViewGroup;

public class ActivityFinder {
    public static Activity findActivity(View view) {
        Activity result = getActivity(view.getContext());
        if (result == null && (view instanceof ViewGroup)) {
            ViewGroup v = (ViewGroup) view;
            int c = v.getChildCount();
            for (int i = 0; i < c && result == null; ++i) {
                result = getActivity(v.getChildAt(i).getContext());
            }
        }
        return result;
    }

    private static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
