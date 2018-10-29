package io.appium.espressoserver.lib.handlers.exceptions;


import android.graphics.Rect;

public class MoveTargetOutOfBoundsException extends AppiumException {

    public MoveTargetOutOfBoundsException(long targetX, long targetY, final Rect boundingRect) {
        super(String.format(
            "The target [%s, %s] for pointer interaction is not in the viewport %s and cannot be brought into the viewport",
            targetX, targetY, boundingRect.toShortString()
        ));
    }

    public MoveTargetOutOfBoundsException(final String message) {
        super(message);
    }
}
