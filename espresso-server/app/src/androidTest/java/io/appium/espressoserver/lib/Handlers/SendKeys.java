package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BadRequestResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InternalErrorResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Element;
import io.appium.espressoserver.lib.Model.Session;

import static android.support.test.espresso.action.ViewActions.typeText;


public class SendKeys extends BaseHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, Object> params) {
        AppiumResponse response = (AppiumResponse)super.handle(session, params);

        String id = (String)params.get("elementId");
        ViewInteraction viewInteraction = Element.getCache().get(id);

        List value = (List)params.get("value");
        String textValue = (String)value.get(0);

        if (viewInteraction != null) {
            try {
                viewInteraction.perform(typeText(textValue));
            } catch (PerformException e) {
                return new BadRequestResponse("Could not apply sendKeys to element " + id + ": " + e.getMessage());
            }
        } else {
            return new BadRequestResponse("Could not find element with ID: " + id);
        }

        response.setAppiumStatus(AppiumStatus.SUCCESS);

        return response;
    }
}
