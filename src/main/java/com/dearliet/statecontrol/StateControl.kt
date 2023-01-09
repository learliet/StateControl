package com.dearliet.statecontrol

import org.bukkit.plugin.java.JavaPlugin

class StateControl : JavaPlugin() {

    companion object {
        lateinit var instance: StateControl
    }

    override fun onEnable() {
        instance = this
    }
    
}