package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.ViewInteraction;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InternalErrorResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Session;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.
import static android.support.test.espresso.matcher.ViewMatchers.withText;;

/**
 * Created by ahmetkocu on 27.05.2017.
 */

public class ScrollTo implements RequestHandler {

    @Override
    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {

        // If the SessionID is invalid, return InvalidSessionResponse
        // TODO: Fix SessionID handling redundancies
        if (!uriParams.get("sessionId").equals(Session.getGlobalSessionId())) {
            return new InvalidSessionResponse(uriParams.get("sessionId"));
        }

        String text = uriParams.get("text").toString();

        AppiumResponse response = new AppiumResponse();
        try {
            ViewInteraction viewInteraction = onView(withText(text));

            if(viewInteraction != null) {
                viewInteraction.perform(scrollTo());
            }
        }
        catch (Exception ex) {
            return new InternalErrorResponse(ex.getMessage());
        }

        response.setAppiumStatus(AppiumStatus.SUCCESS);

        return response;
    }


}
