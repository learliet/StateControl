package com.dearliet.statecontrol

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.BufferedReader
import java.io.FileReader
import java.util.*

class StateControl : JavaPlugin() {

    companion object {
        lateinit var instance: StateControl
    }

    override fun onEnable() {
        instance = this

        EnumeratedExample.GameObject(world.spawnLocation)
    }

    val world by lazy {
        val br = BufferedReader(FileReader("server.properties"))
        val props = Properties()
        props.load(br)
        br.close()
        Bukkit.getWorld(props.getProperty("level-name"))!!
    }
}