package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

import javax.annotation.Nullable;

import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class ScrollTo implements ViewAction {

    private int xOffset;
    private int yOffset;

    public ScrollTo() {
        super();
    }

    public ScrollTo(@Nullable Integer xOffset, @Nullable Integer yOffset) {
        super();
        this.xOffset = xOffset == null ? 0 : xOffset;
        this.yOffset = yOffset == null ? 0 : yOffset;
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
        int x = view.getLeft();
        int y = view.getTop() + view.getHeight();
        view.scrollTo(x, y);
    }

}
