package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.widget.AbsListView;

import org.hamcrest.Matcher;

import javax.annotation.Nullable;

import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class ScrollTo implements ViewAction {

    private int xOffset = 0;
    private int yOffset = 0;

    public ScrollTo() {
        super();
    }

    public ScrollTo(@Nullable Integer xOffset, @Nullable Integer yOffset) {
        super();
        if (xOffset != null) {
            this.xOffset = xOffset;
        }
        if (yOffset != null) {
            this.yOffset = yOffset;
        }
    }

    @Override
    public Matcher<View> getConstraints() {
        // This is a hack constraint that passes any view through
        return isDescendantOfA(isRoot());
    }

    @Override
    public String getDescription() {
        return "getting a view reference";
    }

    @Override
    public void perform(UiController uiController, View view) {
        int x = view.getLeft() + this.xOffset;
        int y = view.getTop() + view.getHeight() + this.yOffset;

        View viewParent = (View) view.getParent();

        if (viewParent instanceof AbsListView) {
            ((AbsListView) viewParent).smoothScrollToPosition(y);
        } else {
            viewParent.scrollTo(x, y);
        }
    }

}
