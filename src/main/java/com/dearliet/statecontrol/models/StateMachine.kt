package com.dearliet.statecontrol.models

import com.dearliet.statecontrol.StateControl
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import kotlin.math.abs

/*
 * TODO:
 *  - Rework this class so that the currentState is of type E
 *  - Rework transitionToState to take parameter of type E
 *  - Consider whether it is useful to make the parentStateMachine non-nullable and use Nothing instead of null as the root state machine
 * */

/**
 * An abstract class representing a state machine.
 *
 * @param holder The unique object that owns the state machine structure.
 * @param parentStateMachine The parent [StateMachine] of this instance.
 * */
abstract class StateMachine<T: Any, E: Enum<E>>(holder: T, parentStateMachine: StateMachine<T, *>? = null)
    : State<T>(holder, parentStateMachine) {

    /**
     * A [LinkedHashMap] that holds the state constructors or factories.
     */
    protected abstract val initStates : LinkedHashMap<E, () -> State<T>>
    /**
     * A [LinkedHashMap] that holds the fully-initialized [State] instances.
     */
    private val states: LinkedHashMap<E, State<T>> by lazy { initStates.mapValues { it.value.invoke() }.toMap() as LinkedHashMap<E, State<T>> }
    /**
     * The key (or enumeration) of the initial [State] of the state machine.
     */
    open val startingStateKey: E by lazy { states.keys.first() }
    /**
     * The key of the current [State] in the state machine.
     */
    lateinit var currentStateKey: E
        private set

    /**
     * Starts the [StateMachine] by transitioning to the [startingStateKey].
     */
    fun start(){
        transitionToState(startingStateKey)
    }

    /**
     * Transitions to the first [State] in the [states] map.
     */
    fun head(){
        transitionToState(states.keys.first())
    }

    /**
     * Transitions to the last [State] in the [states] map.
     */
    fun tail(){
        transitionToState(states.keys.last())
    }

    private fun step(stepCount: Int){
        if(!this::currentStateKey.isInitialized) return

        val index = getIndex(currentStateKey) + stepCount

        if(index >= states.size) {
            onExceedUpperStateBoundary()
            return
        } else if(index < 0) {
            onExceedLowerStateBoundary()
            return
        }

        transitionToState(states.keys.elementAt(index))
    }

    /**
     * Transitions to the next [State] in the [states] map by given [stepCount].
     * @param stepCount The number of steps to take when moving through [states].
     */
    fun next(stepCount: Int = 1){
        step(abs(stepCount))
    }

    /**
     * Transitions to the previous [State] in the [states] map by given [stepCount].
     * @param stepCount The number of steps to take when moving through [states].
     */
    fun previous(stepCount: Int = 1){
        step(-abs(stepCount))
    }

    /**
     * Transitions to the [State] represented by [stateKey].
     * @param stateKey The key of the [State] to transition to.
     */
    fun goto(stateKey: E){
        transitionToState(stateKey)
    }

    /**
     * Called when the [StateMachine] completes.
     */
    protected open fun onComplete(){}

    /**
     * Called when the [StateMachine] changes state
     * @param previousStateKey The key of the [State] that the [StateMachine] was in before transitioning to the current [State].
     */
    protected open fun onChangeState(previousStateKey: E){}

    /**
     * Returns the index of the [stateKey] in the [states] map.
     * @param stateKey The key to get the index of.
     */
    fun getIndex(stateKey: E): Int {
        return states.keys.indexOf(stateKey)
    }

    /**
     * Returns the [State] represented by [stateKey] in the [states] map.
     * @param stateKey The key of the [State] to get.
     */
    fun getState(stateKey: E): State<T>? {
        return states[stateKey]
    }

    /**
     * Triggers [onComplete] and calls [next] on the [parentStateMachine].
     */
    protected fun complete(){
        onComplete()
        parentStateMachine?.next()
    }

    /**
     * Called when the [StateMachine] exceeds the upper bound of [states].
     */
    protected open fun onExceedUpperStateBoundary(){
        complete()
    }

    /**
     * Called when the [StateMachine] exceeds the lower bound of [states].
     */
    protected open fun onExceedLowerStateBoundary(){
        parentStateMachine?.previous()
    }

    private fun loadState(state: State<T>){
        state.onEnable()
        Bukkit.getPluginManager().registerEvents(state, StateControl.instance)
    }

    private fun transitionToState(state: E){
        val isInitialized = this::currentStateKey.isInitialized
        if(!states.containsKey(state) || (isInitialized && currentStateKey == state)) return
        if(!isInitialized && parentStateMachine == null) {
            loadState(this)
        }

        if(isInitialized) {
            val previousStateKey = currentStateKey
            exitCurrentState()
            currentStateKey = state
            onChangeState(previousStateKey)
        } else {
            currentStateKey = startingStateKey
        }

        states[currentStateKey]?.let {
            (it as? StateMachine<T, *>)?.start()
            loadState(it)
        }
    }

    private fun exitCurrentState(){
        val stateModel = states.values.elementAt(getIndex(currentStateKey))

        states[currentStateKey] = initStates.values.elementAt(getIndex(currentStateKey))()

        (stateModel as? StateMachine<T, *>)?.exitCurrentState()
        stateModel.onDisable()
        HandlerList.unregisterAll(stateModel)
    }
}

/*
currentStateIndex = index.coerceIn(0, states.size - 1)

* if(index >= states.size){
            onExceedUpperStateBoundary()
            return
        } else if(index < 0){
            onExceedLowerStateBoundary()
            return
        }
* */