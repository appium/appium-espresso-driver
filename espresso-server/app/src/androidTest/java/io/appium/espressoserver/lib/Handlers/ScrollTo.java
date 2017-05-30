package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.ViewInteraction;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Session;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class ScrollTo extends BaseHandler {

    @Override
    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, Object> params) {

        AppiumResponse response = (AppiumResponse)super.handle(session, params);

        String text = (String)params.get("text");

        ViewInteraction viewInteraction = onView(withText(text));

        if (viewInteraction != null) {
            viewInteraction.perform(scrollTo());
        }

        response.setAppiumStatus(AppiumStatus.SUCCESS);

        return response;
    }


}
