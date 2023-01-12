package com.dearliet.statecontrol.models

import com.dearliet.statecontrol.StateControl
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * An abstract [State] class that counts down from a specified number of [ticks] and transitions automatically to the next [State] once the timer has run out.
 *
 * @param holder The unique object that owns the state machine structure.
 * @param parentStateMachine The parent [StateMachine] of this instance.
 * @param ticks The total number of ticks for the timer.
 */
abstract class TimerState<T: Any>(holder: T, parentStateMachine: StateMachine<T, *>?, open val ticks: Int = 20): State<T>(holder, parentStateMachine) {
    private lateinit var task: BukkitTask

    /**
     * This function is called for each tick.
     * @param tick The number of ticks left before the timer completes.
     */
    open fun onTick(tick: Int){}

    override fun onEnable() {
        super.onEnable()
        var ticksLeft = ticks

        task = object : BukkitRunnable(){

            override fun run() {
                onTick(ticksLeft)

                if(ticksLeft == 0){
                    parentStateMachine?.next()
                }

                ticksLeft--
            }

        }.runTaskTimer(StateControl.instance, 0, 1)
    }

    override fun onDisable() {
        super.onDisable()
        task.cancel()
    }
}