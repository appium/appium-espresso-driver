package io.appium.espressoserver.lib.handlers.exceptions;


public class MoveTargetOutOfBoundsException extends AppiumException {
    public MoveTargetOutOfBoundsException() {
        super("The target for pointer interaction is not in the viewport and cannot be brought into that viewport");
    }

    public MoveTargetOutOfBoundsException(long targetX, long targetY) {
        super(String.format(
            "The target [%s, %s] for pointer interaction is not in the viewport and cannot be brought into that viewport",
            targetX, targetY
        ));
    }
}
