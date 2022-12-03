package net.kunmc.deathcolor

import org.bukkit.ChatColor
import org.bukkit.Material

enum class EnumColor(
    val colorName: String,
    val langName: String,
    val chatColor: ChatColor,
    val wool: Material,
) {
    WHITE("white", "白", ChatColor.WHITE, Material.WHITE_WOOL),
    ORANGE("orange", "橙", ChatColor.GOLD, Material.ORANGE_WOOL),
    MAGENTA("magenta", "赤紫", ChatColor.LIGHT_PURPLE, Material.MAGENTA_WOOL),
    LIGHT_BLUE("light_blue", "空色", ChatColor.AQUA, Material.LIGHT_BLUE_WOOL),
    YELLOW("yellow", "黄", ChatColor.YELLOW, Material.YELLOW_WOOL),
    LIME("lime", "黄緑", ChatColor.GREEN, Material.LIME_WOOL),
    PINK("pink", "桃", ChatColor.LIGHT_PURPLE, Material.PINK_WOOL),
    GRAY("gray", "灰", ChatColor.DARK_GRAY, Material.GRAY_WOOL),
    LIGHT_GRAY("light_gray", "薄灰", ChatColor.GRAY, Material.LIGHT_GRAY_WOOL),
    CYAN("cyan", "青緑", ChatColor.DARK_AQUA, Material.CYAN_WOOL),
    PURPLE("purple", "紫", ChatColor.DARK_PURPLE, Material.PURPLE_WOOL),
    BLUE("blue", "青", ChatColor.BLUE, Material.BLUE_WOOL),
    BROWN("brown", "茶", ChatColor.GOLD, Material.BROWN_WOOL),
    GREEN("green", "緑", ChatColor.DARK_GREEN, Material.GREEN_WOOL),
    RED("red", "赤", ChatColor.RED, Material.RED_WOOL),
    BLACK("black", "黒", ChatColor.BLACK, Material.BLACK_WOOL),
}