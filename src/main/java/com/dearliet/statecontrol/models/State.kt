package com.dearliet.statecontrol.models

import org.bukkit.event.Listener

/**
 * An abstract class representing a state in a [StateMachine].
 *
 * @param holder The unique object that owns the state machine structure.
 * @param parentStateMachine The parent [StateMachine] of this instance.
 */
abstract class State<T: Any>(val holder: T, open val parentStateMachine: StateMachine<T, *>?): Listener {
    /**
     * Called when this [State] is enabled.
     */
    open fun onEnable(){}
    /**
     * Called when this [State] is disabled.
     */
    open fun onDisable(){}
}