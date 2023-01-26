package com.dearliet.statecontrol.models

import com.dearliet.statecontrol.models.BaseState.StateMachine

/**
 * [LinearStateMachine] is a concrete implementation of [StateMachine] that allows for traversing through a set of states in a linear fashion.
 *
 * @param holder The unique object that owns the state machine structure
 * @param parentStateMachine The parent [StateMachine] of this instance.
 */
abstract class LinearStateMachine<T : Any>(holder: T, parentStateMachine: StateMachine<T, *>? = null)
    : StateMachine<T, Int>(holder, parentStateMachine) {

    abstract override val initStates: List<() -> BaseState<T>>

    override val stateFactory: LinkedHashMap<Int, () -> BaseState<T>>
        get() {
            val list = initStates
            val linkedHashMap = LinkedHashMap<Int, () -> BaseState<T>>()
            list.forEachIndexed { index, _ ->
                linkedHashMap[index] = list[index]
            }

            return linkedHashMap
        }

    private fun step(stepCount: Int){
        if(!isActive) return

        val index = states.keys.indexOf(activeStateKey) + stepCount

        if(index >= states.size) {
            onExceedUpperBound()
            return
        } else if(index < 0) {
            onExceedLowerBound()
            return
        }

        transitionTo(states.keys.elementAt(index))
    }

    /**
     * Transitions to the next [BaseState] in the [states] map.
     */
    fun next(){
        step(1)
    }

    /**
     * Transitions to the previous [BaseState] in the [states] map.
     */
    fun previous(){
        step(-1)
    }

    /**
     * Called when [next] is triggered and the state machine detects that the target index is not within the bounds of the [states] map.
     * */
    protected open fun onExceedUpperBound(){}

    /**
     * Called when [previous] is triggered and the state machine detects that the target index is not within the bounds of the [states] map.
     * */
    protected open fun onExceedLowerBound(){}
}