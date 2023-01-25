package com.dearliet.statecontrol.models

import kotlin.properties.Delegates

/**
 * The [ScaleStateMachine] simulates a scale, adjusting its [currentBalance] value when transitioning between states.
 * When transitioning past the upper bounds, [currentBalance] increases, and when transitioning past the lower bounds, it decreases.
 * The state machine will return to the starting state and continue to do so until [currentBalance] reaches the specified [targetBalance].
 */
abstract class ScaleStateMachine<T : Any>(holder: T, parentStateMachine: StateMachine<T, *>? = null)
    : LinearStateMachine<T>(holder, parentStateMachine) {

    protected open fun incrementCondition() : Boolean { return true }
    protected open fun decrementCondition() : Boolean { return true }

    /**
     * The current weight value of the [ScaleStateMachine] that increases when transitioning past the upper bounds and decreases when transitioning past the lower bounds.
     * */
    protected var currentBalance by Delegates.observable(0) { _, oldValue, _ ->
        onBalanceChange(oldValue)
        if (parentStateMachine == null || parentStateMachine.states.containsValue(this)) {
            start()
        }
    }
        private set

    protected open fun onBalanceChange(previousBalance: Int){}

    override fun onExceedUpperBound() {
        super.onExceedUpperBound()
        if(incrementCondition()) currentBalance++
    }

    override fun onExceedLowerBound() {
        super.onExceedLowerBound()
        if(decrementCondition()) currentBalance--
    }
}