package com.dearliet.statecontrol.models

import com.dearliet.statecontrol.StateControl
import com.dearliet.statecontrol.models.BaseState.StateMachine
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/**
 * A base class representing all states and state machines.
 *
 * @param owner The unique object that owns the state machine structure.
 * @param parentStateMachine The parent [StateMachine] of this instance.
 */
sealed class BaseState<T: Any>(protected val owner: T, open val parentStateMachine: StateMachine<T, *>?) : Listener {
    /**
     * Called when base state is enabled.
     */
    protected open fun onEnable(){}
    /**
     * Called when base state is disabled.
     */
    protected open fun onDisable(){}

    /**
     * An abstract class representing a state machine.
     *
     * @param owner The unique object that owns the state machine structure.
     * @param parentStateMachine The parent [StateMachine] of this instance.
     * */
    sealed class StateMachine<T: Any, E: Any>(owner: T, parentStateMachine: StateMachine<T, *>?)
        : BaseState<T>(owner, parentStateMachine) {

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
        internal var isActive = false

        /**
         * Transitions to the [BaseState] represented by the [startingStateKey] in the [states] map.
         *
         * @throws NoSuchElementException if the [states] map is empty.
         * @throws IllegalArgumentException if the [startingStateKey] is invalid.
         */
        fun start(){
            transitionTo(startingStateKey)
        }

        /**
         * Called when the [StateMachine] transitions to another [BaseState].
         * @param previousStateKey The key of the [BaseState] that the [StateMachine] was in before transitioning to the current [BaseState].
         */
        protected open fun onChangeState(previousStateKey: E){}

        internal fun transitionTo(stateKey: E){
            if(states.isEmpty()) throw NoSuchElementException("Unable to start the state machine because it does not contain any states.")
            require(states.containsKey(stateKey)) { "Invalid stateKey provided, expected one of ${states.keys} but got $stateKey." }

            if(isActive){
                if(activeStateKey == stateKey) return
                val previousStateKey = activeStateKey
                exitCurrentState()
                activeStateKey = stateKey
                onChangeState(previousStateKey)
            } else {
                if(parentStateMachine == null) loadState(this)
                activeStateKey = startingStateKey
                isActive = true
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

        private fun unloadState(state: BaseState<T>){
            state.onDisable()
            HandlerList.unregisterAll(state)
        }

        /**
         * Cancels the *root* state machine and unregisters all associated event listeners.
         */
        fun cancelRoot(){
            var root: StateMachine<T, *> = this
            while(root.parentStateMachine != null){
                root = root.parentStateMachine!!
            }

            if(!root.isActive) return

            root.apply {
                exitCurrentState()
                unloadState(this)
                isActive = false
            }
        }

        private fun exitCurrentState(){
            val currentStateIndex = states.keys.indexOf(activeStateKey)
            val stateModel = states.values.elementAt(currentStateIndex)
            states[activeStateKey] = stateFactory.values.elementAt(currentStateIndex)()
            (stateModel as? StateMachine<T, *>)?.exitCurrentState()
            unloadState(stateModel)
        }
    }
}

/**
 * An abstract class representing a state in a [StateMachine].
 *
 * @param owner The unique object that owns the state machine structure.
 * @param parentStateMachine The parent [StateMachine] of this instance.
 */
abstract class State<T : Any>(owner: T, parentStateMachine: StateMachine<T, *>) : BaseState<T>(owner, parentStateMachine)