package com.dearliet.statecontrol

import org.bukkit.plugin.java.JavaPlugin

object StateControl {

    private lateinit var instance: JavaPlugin

    fun setImplementation(plugin: JavaPlugin){
        instance = plugin
    }

    internal fun getInstance() : JavaPlugin {
        if(!this::instance.isInitialized) throw IllegalStateException("No implementation set.")
        return instance
    }
}