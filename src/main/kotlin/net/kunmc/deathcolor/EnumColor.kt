package net.kunmc.deathcolor

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.boss.BarColor

enum class EnumColor(
    val colorName: String,
    val langName: String,
    val chatColor: ChatColor,
    val wool: Material,
    val barColor: BarColor,
) {
    WHITE("white", "白", ChatColor.WHITE, Material.WHITE_WOOL, BarColor.WHITE),
    ORANGE("orange", "橙", ChatColor.GOLD, Material.ORANGE_WOOL, BarColor.YELLOW),
    MAGENTA("magenta", "赤紫", ChatColor.LIGHT_PURPLE, Material.MAGENTA_WOOL, BarColor.PINK),
    LIGHT_BLUE("light_blue", "空色", ChatColor.AQUA, Material.LIGHT_BLUE_WOOL, BarColor.BLUE),
    YELLOW("yellow", "黄", ChatColor.YELLOW, Material.YELLOW_WOOL, BarColor.YELLOW),
    LIME("lime", "黄緑", ChatColor.GREEN, Material.LIME_WOOL, BarColor.GREEN),
    PINK("pink", "桃", ChatColor.LIGHT_PURPLE, Material.PINK_WOOL, BarColor.PINK),
    // LIGHT_GRAYはGRAYを含んでいてcontainsでマッチしてしまうため、LIGHT_GRAYとGRAYを入れ替えておく
    LIGHT_GRAY("light_gray", "薄灰", ChatColor.GRAY, Material.LIGHT_GRAY_WOOL, BarColor.WHITE),
    GRAY("gray", "灰", ChatColor.DARK_GRAY, Material.GRAY_WOOL, BarColor.WHITE),
    CYAN("cyan", "青緑", ChatColor.DARK_AQUA, Material.CYAN_WOOL, BarColor.BLUE),
    PURPLE("purple", "紫", ChatColor.DARK_PURPLE, Material.PURPLE_WOOL, BarColor.PINK),
    BLUE("blue", "青", ChatColor.BLUE, Material.BLUE_WOOL, BarColor.BLUE),
    BROWN("brown", "茶", ChatColor.GOLD, Material.BROWN_WOOL, BarColor.RED),
    GREEN("green", "緑", ChatColor.DARK_GREEN, Material.GREEN_WOOL, BarColor.GREEN),
    RED("red", "赤", ChatColor.RED, Material.RED_WOOL, BarColor.RED),
    BLACK("black", "黒", ChatColor.BLACK, Material.BLACK_WOOL, BarColor.WHITE),
}