package io.appium.espressoserver.lib.handlers;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import androidx.test.espresso.ViewInteraction;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.ViewAttributesEnum;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.ViewTextGetter;

public class GetAttribute implements RequestHandler<AppiumParams, String> {

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
                    String.format("Attribute name should be one of %s. '%s' is given instead",
                            supportedAttributeNames, attributeName));
        }
        final ViewInteraction viewInteraction = Element.getViewInteractionById(params.getElementId());
        final View view = Element.getViewById(params.getElementId());
        if (dstAttribute == ViewAttributesEnum.TEXT) {
            return new ViewTextGetter().get(viewInteraction).getRawText();
        }
        final ViewElement viewElement = new ViewElement(view);
        switch (dstAttribute) {
            case CONTENT_DESC:
                return viewElement.getContentDescription() == null ?
                        null :
                        viewElement.getContentDescription().toString();
            case CLASS:
                return viewElement.getClassName();
            case CHECKABLE:
                return Boolean.toString(viewElement.isCheckable());
            case CHECKED:
                return Boolean.toString(viewElement.isChecked());
            case CLICKABLE:
                return Boolean.toString(viewElement.isClickable());
            case ENABLED:
                return Boolean.toString(viewElement.isEnabled());
            case FOCUSABLE:
                return Boolean.toString(viewElement.isFocusable());
            case FOCUSED:
                return Boolean.toString(viewElement.isFocused());
            case SCROLLABLE:
                return Boolean.toString(viewElement.isScrollable());
            case LONG_CLICKABLE:
                return Boolean.toString(viewElement.isLongClickable());
            case PASSWORD:
                return Boolean.toString(viewElement.isPassword());
            case SELECTED:
                return Boolean.toString(viewElement.isSelected());
            case VISIBLE:
                return Boolean.toString(viewElement.isVisible());
            case BOUNDS:
                return viewElement.getBounds().toShortString();
            case RESOURCE_ID:
                return viewElement.getResourceId();
            case INDEX:
                return Integer.toString(viewElement.getIndex());
            case PACKAGE:
                return viewElement.getPackageName();
            case VIEW_TAG:
                return viewElement.getViewTag();
            default:
                throw new NotYetImplementedException();
        }
    }
}