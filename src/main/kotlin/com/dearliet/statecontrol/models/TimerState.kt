package com.dearliet.statecontrol.models

import com.dearliet.statecontrol.StateControl
import com.dearliet.statecontrol.models.BaseState.StateMachine
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * The [TimerState] class counts down from a specified number of [units] and allows an action to be specified once the timer has run out.
 *
 * @param owner The unique object that owns the state machine structure.
 * @param parentStateMachine The parent [StateMachine] of this instance.
 */
abstract class TimerState<T : Any>(owner: T, parentStateMachine: StateMachine<T, *>)
    : State<T>(owner, parentStateMachine) {

    private lateinit var task: BukkitTask
    /**
     * The number of units in the timer.
     */
    abstract val units: Long
    /**
     * The number of ticks per unit.
     */
    open val ticksPerUnit: Long = 1

    /**
     * This function is called for each unit.
     * @param unitsLeft The number of units left before the timer completes.
     */
    open fun onUnit(unitsLeft: Long){}

    /**
     * This function is called when the timer completes.
     */
    open fun onComplete(){}

    override fun onEnable() {
        super.onEnable()
        var unitsLeft = units
        task = object : BukkitRunnable(){

            override fun run() {
                onUnit(unitsLeft)
                Bukkit.getLogger().info(unitsLeft.toString())
                if(unitsLeft-- <= 0) onComplete()
            }

        }.runTaskTimer(StateControl.instance, 0, ticksPerUnit)
    }

    override fun onDisable() {
        super.onDisable()
        task.cancel()
    }
}
