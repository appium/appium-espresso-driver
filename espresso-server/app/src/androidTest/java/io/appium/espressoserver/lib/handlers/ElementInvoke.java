package io.appium.espressoserver.lib.handlers;

import android.view.View;

import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.BackdoorUtils;
import io.appium.espressoserver.lib.helpers.InvocationOperation;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.ElementInvokeParams;

public class ElementInvoke implements RequestHandler<ElementInvokeParams, Object> {
    @Override
    public Object handle(ElementInvokeParams params) throws AppiumException {
        View view = Element.getViewById(params.getElementId());
        List<InvocationOperation> ops = BackdoorUtils.getOperations(params.getMethods());
        return BackdoorUtils.invokeMethods(view, ops);
    }
}
