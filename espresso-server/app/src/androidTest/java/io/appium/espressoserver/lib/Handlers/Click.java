/*package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.ViewInteraction;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BadRequestResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Element;

import static android.support.test.espresso.action.ViewActions.click;

public class Click extends BaseHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, Object> params) {
        AppiumResponse response = (AppiumResponse)super.handle(session, params);

        String id = (String)params.get("elementId");
        ViewInteraction viewInteraction = Element.getCache().get(id);

        if (viewInteraction != null) {
            try {
                viewInteraction.perform(click());
                response.setAppiumStatus(AppiumStatus.SUCCESS);
            } catch (Exception e) {
                return new BadRequestResponse("Could not find element with ID: " + id);
            }
        } else {
            return new BadRequestResponse("Could not find element with ID: " + id);
        }

        return response;
    }
}
*/