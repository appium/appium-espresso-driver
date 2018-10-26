package io.appium.espressoserver.lib.handlers.exceptions;


public class MoveTargetOutOfBoundsException extends AppiumException {

    public MoveTargetOutOfBoundsException(long targetX, long targetY, long viewportWidth, long viewportHeight) {
        super(String.format(
            "The target [%s, %s] for pointer interaction is not in the viewport [%s, %s] and cannot be brought into the viewport",
            targetX, targetY, viewportWidth, viewportHeight
        ));
    }
}
