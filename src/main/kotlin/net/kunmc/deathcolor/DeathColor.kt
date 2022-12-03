package net.kunmc.deathcolor

import org.bukkit.plugin.java.JavaPlugin

class DeathColor : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        ColorMapGenerator.generateBlockToColor()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}