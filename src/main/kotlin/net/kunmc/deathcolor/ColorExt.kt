package net.kunmc.deathcolor

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.*

/** アイテムの色を取得する */
fun Material.toEnumColor(): EnumColor? {
    // ブロックの色をconfigから取得
    val colorName = DeathColor.instance.materialToColor.getString(name)
    return EnumColor.values().find { it.colorName == colorName }
}

/** エンティティの色を取得する */
fun Entity.toEnumColor(): EnumColor? {
    val entity = when (type) {
        EntityType.ITEM_FRAME -> {
            this as ItemFrame
            return this.item.type.toEnumColor()
        }

        EntityType.FALLING_BLOCK -> {
            this as FallingBlock
            return this.blockData.material.toEnumColor()
        }

        EntityType.DROPPED_ITEM -> {
            this as Item
            return this.itemStack.type.toEnumColor()
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
