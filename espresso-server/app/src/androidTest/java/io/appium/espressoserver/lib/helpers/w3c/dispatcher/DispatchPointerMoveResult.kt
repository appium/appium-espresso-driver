package io.appium.espressoserver.lib.helpers.w3c.dispatcher

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState

class DispatchPointerMoveResult(private val dispatcherAdapter: W3CActionAdapter,
                                private val sourceId: String,
                                private val pointerType: InputSource.PointerType,
                                private val currentX: Float, private val currentY: Float,
                                private val x: Float, private val y: Float,
                                private val buttons: Set<Int>,
                                private val globalKeyInputState: KeyInputState?) : BaseDispatchResult() {
    @Throws(AppiumException::class)
    override fun perform() {
        if (currentX != x || currentY != y) {
            dispatcherAdapter.pointerMove(sourceId, pointerType, currentX, currentY, x, y, buttons, globalKeyInputState)
        }
    }

}