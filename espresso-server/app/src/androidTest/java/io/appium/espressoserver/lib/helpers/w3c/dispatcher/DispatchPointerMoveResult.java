package io.appium.espressoserver.lib.helpers.w3c.dispatcher;

import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

public class DispatchPointerMoveResult extends BaseDispatchResult {

    private W3CActionAdapter dispatcherAdapter;
    private String sourceId;
    private InputSource.PointerType pointerType;
    private long currentX;
    private long currentY;
    private long x;
    private long y;
    private Set<Integer> buttons;
    private KeyInputState globalKeyInputState;

    public DispatchPointerMoveResult(final W3CActionAdapter dispatcherAdapter,
                                     final String sourceId,
                                     final InputSource.PointerType pointerType,
                                     final long currentX, final long currentY,
                                     final long x, final long y,
                                     final Set<Integer> buttons,
                                     final KeyInputState globalKeyInputState) {
        this.dispatcherAdapter = dispatcherAdapter;
        this.sourceId = sourceId;
        this.pointerType = pointerType;
        this.currentX = currentX;
        this.currentY = currentY;
        this.x = x;
        this.y = y;
        this.buttons = buttons;
        this.globalKeyInputState = globalKeyInputState;
    }

    public void perform() throws AppiumException {
        if (currentX != x || currentY != y) {
            dispatcherAdapter.pointerMove(sourceId, pointerType, currentX, currentY, x, y, buttons, globalKeyInputState);
        }
    }

}
