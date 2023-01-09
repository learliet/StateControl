package com.dearliet.statecontrol.models

import com.dearliet.statecontrol.StateControl
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

abstract class TimerState<T: Any>(holder: T, parentStateMachine: StateMachine<T>?, ticks: Int): State<T>(holder, parentStateMachine) {
    private lateinit var task: BukkitTask
    val totalTicks: Int = ticks

    open fun onTick(tick: Int){}

    override fun onEnable() {
        super.onEnable()
        var ticksLeft = totalTicks

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