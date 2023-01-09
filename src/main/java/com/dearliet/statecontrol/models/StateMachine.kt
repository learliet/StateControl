package com.dearliet.statecontrol.models

import com.dearliet.statecontrol.StateControl
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import kotlin.math.abs

@Suppress("LeakingThis")
abstract class StateMachine<T : Any>(holder: T, parentStateMachine: StateMachine<T>? = null): State<T>(holder, parentStateMachine) {
    protected abstract val states: List<State<T>>
    var currentStateIndex = -1
        private set
    var currentState: State<T>? = null
        private set
    override val name: String
        get() = javaClass.simpleName + "/" + currentState?.name

    fun start(){
        transitionToState(0)
    }

    fun next(step: Int = 1){
        transitionToState(currentStateIndex + abs(step))
    }

    fun previous(step: Int = 1){
        transitionToState(currentStateIndex - abs(step))
    }

    protected open fun onComplete(){}
    protected open fun onChangeState(){}

    protected open fun onExceedUpperStateBoundary(){
        onComplete()
        parentStateMachine?.next()
    }

    protected open fun onExceedLowerStateBoundary(){
        parentStateMachine?.previous()
    }

    private fun transitionToState(index: Int){
        if(states.isEmpty() || currentStateIndex == index) return
        currentStateIndex = index.coerceIn(0, states.size - 1)

        if(index >= states.size){
            onExceedUpperStateBoundary()
            return
        } else if(index < 0){
            onExceedLowerStateBoundary()
            return
        }

        currentState?.let {
            exitState(it)
            onChangeState()
        }
        currentState = states[currentStateIndex]
        (currentState as? StateMachine)?.start()
        currentState?.onEnable()
        Bukkit.getPluginManager().registerEvents(currentState!!, StateControl.instance)
    }

    private fun exitState(state: State<T>){
        (state as? StateMachine)?.exitState(state.currentState!!)
        state.onDisable()
        HandlerList.unregisterAll(state)
    }
}