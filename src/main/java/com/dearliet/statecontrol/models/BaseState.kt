package com.dearliet.statecontrol.models

import com.dearliet.statecontrol.StateControl
import com.dearliet.statecontrol.models.BaseState.StateMachine
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/**
 * Abstract class representing all states and state machines.
 *
 * @param holder The unique object that owns the state machine structure.
 * @param parentStateMachine The parent [StateMachine] of this instance.
 */
sealed class BaseState<T: Any>(protected val holder: T, open val parentStateMachine: StateMachine<T, *>?) : Listener {
    /**
     * Called when this [BaseState] is enabled.
     */
    protected open fun onEnable(){}
    /**
     * Called when this [BaseState] is disabled.
     */
    protected open fun onDisable(){}

    /**
     * An abstract class representing a state machine.
     *
     * @param holder The unique object that owns the state machine structure.
     * @param parentStateMachine The parent [StateMachine] of this instance.
     * */
    sealed class StateMachine<T: Any, E: Any>(holder: T, parentStateMachine: StateMachine<T, *>?)
        : BaseState<T>(holder, parentStateMachine) {

        protected abstract val initStates: Any
        /**
         * A [LinkedHashMap] that holds the state constructors or factories.
         */
        internal abstract val stateFactory : LinkedHashMap<E, () -> BaseState<T>>
        /**
         * A [LinkedHashMap] that holds the fully-initialized [BaseState] instances.
         */
        internal val states: LinkedHashMap<E, BaseState<T>> by lazy { stateFactory.mapValues { it.value.invoke() }.toMap() as LinkedHashMap<E, BaseState<T>> }
        /**
         * The key (or enumeration) of the initial [BaseState] of the state machine.
         */
        protected open val startingStateKey: E get() = states.keys.first()
        /**
         * Represents the currently active state in the [StateMachine].
         */
        protected lateinit var activeStateKey: E
            private set
        /**
         * Returns whether the [StateMachine] is active.
         * */
        internal val isActive get() = this::activeStateKey.isInitialized

        /**
         * Transitions to the [BaseState] represented by the [startingStateKey] in the [states] map.
         */
        fun start(){
            transitionTo(startingStateKey)
        }

        /**
         * Called when the [StateMachine] changes state
         * @param previousStateKey The key of the [BaseState] that the [StateMachine] was in before transitioning to the current [BaseState].
         */
        protected open fun onChangeState(previousStateKey: E){}

        internal fun transitionTo(stateKey: E){
            require(states.containsKey(stateKey)) { "Invalid stateKey provided, expected one of ${states.keys} but got $stateKey." }

            if(isActive){
                if(activeStateKey == stateKey) return
                val previousStateKey = activeStateKey
                cancel()
                activeStateKey = stateKey
                onChangeState(previousStateKey)
            } else {
                if(parentStateMachine == null) loadState(this)
                activeStateKey = startingStateKey
            }

            states[activeStateKey]?.let {
                (it as? StateMachine<T, *>)?.start()
                loadState(it)
            }
        }

        private fun loadState(state: BaseState<T>){
            state.onEnable()
            Bukkit.getPluginManager().registerEvents(state, StateControl.instance)
        }

        fun cancel(){
            val currentStateIndex = states.keys.indexOf(activeStateKey)
            val stateModel = states.values.elementAt(currentStateIndex)
            states[activeStateKey] = stateFactory.values.elementAt(currentStateIndex)() // move this to enter state
            (stateModel as? StateMachine<T, *>)?.cancel()
            stateModel.onDisable()
            HandlerList.unregisterAll(stateModel)
        }
    }

    /**
     * An abstract class representing a state in a [StateMachine].
     *
     * @param holder The unique object that owns the state machine structure.
     * @param parentStateMachine The parent [StateMachine] of this instance.
     */
    abstract class State<T : Any>(holder: T, parentStateMachine: StateMachine<T, *>) : BaseState<T>(holder, parentStateMachine)
}

