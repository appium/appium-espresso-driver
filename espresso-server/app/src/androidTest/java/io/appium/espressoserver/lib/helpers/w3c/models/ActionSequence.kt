package io.appium.espressoserver.lib.helpers.w3c.models

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable

import io.appium.espressoserver.lib.helpers.w3c.processor.ActionsProcessor.processSourceActionSequence

/**
 * The algorithm for extracting an action sequence from a request takes the JSON Object representing
 * an action sequence, validates the input, and returns a data structure that is the transpose of
 * the input JSON, such that the actions to be performed in a single tick are grouped together
 *
 * (Defined in 17.3 of spec 'extract an action sequence')
 */
class ActionSequence @Throws(InvalidArgumentException::class, NotYetImplementedException::class)
constructor(actions: Actions, activeInputSources: ActiveInputSources,
            inputStateTable: InputStateTable) : Iterator<Tick> {

    private val ticks = arrayListOf<Tick>()
    private var tickCounter = 0

    init {
        // Check if null to keep Codacy happy. It will never make it this far if it's null though.
        actions.actions?.let {
            for (inputSource in it) {
                val actionObjects = processSourceActionSequence(inputSource, activeInputSources, inputStateTable)
                for ((tickIndex, action) in actionObjects.withIndex()) {
                    if (ticks.size == tickIndex) {
                        ticks.add(Tick())
                    }
                    val tick = ticks[tickIndex]
                    tick.addAction(action)
                }
            }
        }
    }

    override fun hasNext(): Boolean {
        return tickCounter < ticks.size
    }

    override fun next(): Tick {
        return ticks[tickCounter++]
    }

    /**
     * Call the dispatchAll algorithm defined in 17.4
     * @param adapter W3C ActionsPerformer adapter
     * @param inputStateTable Input states for this session
     * @throws AppiumException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Throws(AppiumException::class, InterruptedException::class, ExecutionException::class)
    fun dispatch(adapter: W3CActionAdapter, inputStateTable: InputStateTable) {
        for ((tickIndex, tick) in ticks.withIndex()) {
            val timeAtBeginningOfTick = System.currentTimeMillis()
            val tickDuration = tick.calculateTickDuration()

            // 1. Dispatch all of the events
            adapter.logger.info("Dispatching tick #${tickIndex + 1} of ${ticks.size}")
            val callables = tick.dispatchAll(adapter, inputStateTable, tickDuration)
            var callableCount = callables.size

            adapter.sychronousTickActionsComplete()

            // 2. Wait until the following conditions are all met:

            //  2.1 Wait for any pending async operations
            if (callables.isNotEmpty()) {
                val executor = Executors.newCachedThreadPool()
                val completionService = ExecutorCompletionService<BaseDispatchResult>(executor)
                for (callable in callables) {
                    completionService.submit(callable)
                }

                var received = 0
                while (received < callableCount) {
                    val resultFuture = completionService.take() //blocks if none available
                    val dispatchResult = resultFuture.get()
                    dispatchResult.perform()
                    if (dispatchResult.hasNext()) {
                        callableCount++
                        completionService.submit(dispatchResult.next)
                    }
                    received++
                }
            }

            //  2.2 At least tick duration milliseconds have passed
            val timeSinceBeginningOfTick = (System.currentTimeMillis() - timeAtBeginningOfTick).toFloat()
            if (timeSinceBeginningOfTick < tickDuration) {
                val timeToSleep = tickDuration - timeSinceBeginningOfTick
                adapter.logger.info("Wait for tick to finish for $timeToSleep ms")
                adapter.sleep(timeToSleep)
            }

            // 2.3 The UI thread is complete
            adapter.waitForUiThread()

        }
    }
}
