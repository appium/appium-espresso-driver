package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.ViewAttributesEnum;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.ViewFinder;
import io.appium.espressoserver.lib.viewaction.ViewTextGetter;

public class AttributesReader implements RequestHandler<AppiumParams, String> {

    @Override
    @Nullable
    public String handle(AppiumParams params) throws AppiumException {
        final String attributeName = params.getUriParameterValue("name");
        if (attributeName == null || attributeName.trim().isEmpty()) {
            throw new AppiumException("Attribute name cannot be null or empty");
        }
        ViewAttributesEnum dstAttribute = null;
        for (ViewAttributesEnum attribute : ViewAttributesEnum.values()) {
            if (attributeName.equalsIgnoreCase(attribute.toString())) {
                dstAttribute = attribute;
                break;
            }
        }
        if (dstAttribute == null) {
            final List<String> supportedAttributeNames = new ArrayList<>();
            for (ViewAttributesEnum attribute : ViewAttributesEnum.values()) {
                supportedAttributeNames.add(attribute.toString());
            }
            throw new AppiumException(
                    String.format("Attribute name should one of %s. %s is given instead",
                            supportedAttributeNames, attributeName));
        }
        final ViewInteraction viewInteraction = Element.getById(params.getElementId());
        if (dstAttribute == ViewAttributesEnum.TEXT) {
            return new ViewTextGetter().get(viewInteraction).toString();
        }
        final View view = new ViewFinder().getView(viewInteraction);
        switch (dstAttribute) {
            case CONTENT_DESC:
                return new ViewElement(view).getContentDescription().toString();
            case CLASS:
                return new ViewElement(view).getClassName();
            case CHECKABLE:
                return Boolean.toString(new ViewElement(view).isCheckable());
            case CHECKED:
                return Boolean.toString(new ViewElement(view).isChecked());
            case CLICKABLE:
                return Boolean.toString(new ViewElement(view).isClickable());
            case ENABLED:
                return Boolean.toString(new ViewElement(view).isEnabled());
            case FOCUSABLE:
                return Boolean.toString(new ViewElement(view).isFocusable());
            case FOCUSED:
                return Boolean.toString(new ViewElement(view).isFocused());
            case SCROLLABLE:
                return Boolean.toString(new ViewElement(view).isScrollable());
            case LONG_CLICKABLE:
                return Boolean.toString(new ViewElement(view).isLongClickable());
            case PASSWORD:
                return Boolean.toString(new ViewElement(view).isPassword());
            case SELECTED:
                return Boolean.toString(new ViewElement(view).isSelected());
            case BOUNDS:
                return new ViewElement(view).getBounds().toShortString();
            case RESOURCE_ID:
                return Integer.toString(new ViewElement(view).getResourceId());
            case INDEX:
                return Integer.toString(new ViewElement(view).getIndex());
            // case PACKAGE:
            // case INSTANCE:
            default:
                throw new NotYetImplementedException();
        }
    }
}