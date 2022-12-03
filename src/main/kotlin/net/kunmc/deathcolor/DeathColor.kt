package net.kunmc.deathcolor

import org.bukkit.plugin.java.JavaPlugin

class DeathColor : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        ColorMapGenerator.generateBlockToColor()
        ColorMapGenerator.generateEntityToColor()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}