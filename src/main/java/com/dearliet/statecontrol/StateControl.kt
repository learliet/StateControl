package com.dearliet.statecontrol

import com.dearliet.statecontrol.models.ExampleStateMachine
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.BufferedReader
import java.io.FileReader
import java.util.*

class StateControl : JavaPlugin() {

    /*
    * TODO:
    *   - TimerState Delay
    *   - Maybe get rid of ScaleStateMachine; it's just simpler that way and while it may get rid of boilerplate, it might be a good/simple example without it for the github wiki
    *     - doing round counting and everything manually and cancelling the counting if the round fails is probably the better option in terms of readability, and the github example's primary purpose is to provide a easy to read real-life example, so just do that then, it will be enough and prevent you from eating away more time, also less frustrating!
    * - re-initiate state class when it is re-entered, not when it's exited (makes more sense)
    * */

    companion object {
        lateinit var instance: StateControl
    }

    override fun onEnable() {
        instance = this
        ExampleStateMachine(this).start()
    }

    private val world by lazy {
        val br = BufferedReader(FileReader("server.properties"))
        val props = Properties()
        props.load(br)
        br.close()
        Bukkit.getWorld(props.getProperty("level-name"))!!
    }
}