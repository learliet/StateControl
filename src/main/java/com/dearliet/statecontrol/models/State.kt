package com.dearliet.statecontrol.models

import org.bukkit.Bukkit
import org.bukkit.event.Listener

/**
 * Abstract class representing a state in a state machine.
 *
 * @param holder The object that owns this state.
 * @param parentStateMachine The state machine to associate this state with.
 */
abstract class State<T: Any>(val holder: T, val parentStateMachine: StateMachine<T>?): Listener {
    /**
     * The name of this state. The default implementation returns the simple name of the class.
     */
    open val name: String get() = javaClass.simpleName
    /**
     * Called when this state is enabled.
     */
    open fun onEnable(){
        Bukkit.getLogger().info("$name enabled")
    }
    /**
     * Called when this state is disabled.
     */
    open fun onDisable(){
        Bukkit.getLogger().info("$name disabled")
    }
}