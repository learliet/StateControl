package com.dearliet.statecontrol.models

/**
 * A [StateMachine] that loops through a set of states, starting again at the beginning once it reaches the end.
 * @param holder The unique object that owns the state machine structure.
 * @param parentStateMachine The parent [StateMachine] of this instance.
 * @param loopCount The number of loops the [StateMachine] should go through before stopping. A value of 0 or less will cause the [StateMachine] to loop indefinitely.
 */
abstract class LoopingStateMachine<T : Any, E: Enum<E>>(holder: T, parentStateMachine: StateMachine<T, *>?, open val loopCount: Int = 0)
    : StateMachine<T, E>(holder, parentStateMachine) {

    /**
     * Returns whether the [currentLoop] is the last loop.
     */
    val isLastLoop get() = currentLoop == loopCount

    /**
     * Represents the current loop of the [LoopingStateMachine].
     */
    var currentLoop = 1
        private set

    /**
     * Resets the [currentLoop] counter and transitions to the [State] represented by the [startingStateKey] within [states].
     */
    fun restart(){
        currentLoop = 1
        start()
    }

    /**
     * Called when a loop is completed.
     */
    open fun onLoopComplete(){}

    override fun onExceedUpperStateBoundary() {
        onLoopComplete()
        if(++currentLoop > loopCount && loopCount > 0) {
            super.onExceedUpperStateBoundary()
        } else start()
    }
}