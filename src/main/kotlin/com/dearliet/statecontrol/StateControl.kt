package com.dearliet.statecontrol

import org.bukkit.plugin.java.JavaPlugin

internal class StateControl : JavaPlugin() {

    companion object {
        lateinit var instance: StateControl
    }

    override fun onEnable() {
        instance = this
    }
}