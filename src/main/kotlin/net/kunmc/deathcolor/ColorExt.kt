package net.kunmc.deathcolor

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.*

/** チャットのプレフィックス */
val CHAT_PREFIX = "${ChatColor.GRAY}◆${ChatColor.RESET}"

/** アイテムの色を取得する */
fun Material.toEnumColor(): EnumColor? {
    // ブロックの色をconfigから取得
    val colorName = DeathColor.instance.materialToColor.getString(name)
    return EnumColor.values().find { it.colorName == colorName }
}

/** エンティティの色を取得する */
fun Entity.toEnumColor(): EnumColor? {
    val entity = when (type) {
        // 額縁は中のアイテムの色を使用
        EntityType.ITEM_FRAME -> {
            this as ItemFrame
            return this.item.type.toEnumColor()
        }

        // 落下中のブロックはブロックの色を使用
        EntityType.FALLING_BLOCK -> {
            this as FallingBlock
            return this.blockData.material.toEnumColor()
        }

        // ドロップアイテムはアイテムの色を使用
        EntityType.DROPPED_ITEM -> {
            this as Item
            return this.itemStack.type.toEnumColor()
        }

        // ヒツジは毛の色を使用
        EntityType.SHEEP -> {
            this as Sheep
            return EnumColor.values().find { it.name == color?.name }
        }

        // 色付きシュルカー
        EntityType.SHULKER -> {
            this as Shulker
            return EnumColor.values().find { it.name == color?.name }
        }

        else -> type
    }

    // エンティティの色をconfigから取得
    val colorName = DeathColor.instance.entityToColor.getString(entity.name)
    return EnumColor.values().find { it.colorName == colorName }
}

/** 色を文字列にする */
val EnumColor?.colorText get() = "${this?.chatColor ?: ""}${this?.langName ?: "無"}"

/** ブロックを文字列にする */
val Material.text get() = Component.translatable(translationKey)

/** エンティティを文字列にする */
val Entity.text get() = Component.translatable("entity.minecraft.${type.key.key}")

/** 分,秒の文字列を返す */
val Int.minuteSecondString: String
    get() {
        val minute = this / 60
        val second = this % 60
        // 1分未満の場合は秒のみ表示、秒数が0の場合は分のみ表示
        return if (minute == 0) {
            "${second}秒"
        } else if (second == 0) {
            "${minute}分"
        } else {
            "${minute}分${second}秒"
        }
    }

