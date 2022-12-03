package net.kunmc.deathcolor

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.io.FileReader

/**
 * 色の対応表を作る
 */
object ColorMapGenerator {
    /** キーを変換 */
    private fun convertMaterialKey(key: String): String {
        return key
            .removeSuffix("_slab")
            .removeSuffix("_stairs")
            .removeSuffix("_wall")
            .removeSuffix("_fence_gate")
            .removeSuffix("_fence")
            .removeSuffix("_button")
            .removeSuffix("_pressure_plate")
            .removeSuffix("_wall_sign")
            .removePrefix("potted_")
            .removePrefix("infested_")
            .replace(Regex("_wall_fan$"), "_fan")
            .replace(Regex("_hyphae$"), "_stem")
            .replace(Regex("_wood$"), "_log")
    }

    /** ブロック→色の対応表を生成する */
    fun generateBlockToColor() {
        // テクスチャ→色の対応表を読み込む
        val textureToColor =
            Gson().fromJson<Map<String, String>>(FileReader("../block_colors/texture_to_color.json"), Map::class.java)

        // ブロックのテクスチャを取得
        val map = Material.values().associate { material ->
            // ブロックのバニラID
            val key = convertMaterialKey(material.key.key)

            // 名前
            val name = material.name

            // 対応する色を取得
            val color =
                // 色の名前を含んでいたらその色にする
                EnumColor.values().map { it.colorName }.firstOrNull { key.contains(it) }
                // アイテムのテクスチャを含んでいたらその色にする
                    ?: textureToColor.filterKeys { it.startsWith("item/$key") }.values.firstOrNull()
                    // ブロックのテクスチャを含んでいたらその色にする
                    ?: textureToColor.filterKeys { it.startsWith("block/$key") }.values.firstOrNull()
                    // エンティティの名前を含んでいたらその色にする
                    ?: EntityType.values()
                        .filter { it != EntityType.UNKNOWN && it.isAlive }
                        .map { it.key.key }
                        .firstOrNull { key.contains(it) }
                        ?.let { entityKeyRaw ->
                            // エンティティの名前から変換
                            val entityKey = convertEntityToColor(entityKeyRaw)
                            textureToColor.filterKeys { it.startsWith("entity/$entityKey") }.values.firstOrNull()
                                ?: textureToColor.filterKeys { it.contains(entityKey) }.values.firstOrNull()
                                ?: "null"
                        }
                    ?: "null"

            // 対応
            println("$name\t${material.key.key}\t$color")
            name to color
        }

        // YAMLファイルに保存する
        val yaml = YamlConfiguration()
        map.forEach { (key, value) ->
            yaml.set(key, value)
        }
        yaml.save("../block_colors/material_to_color.yml")
    }

    /** キーを変換 */
    private fun convertEntityToColor(key: String): String {
        return key
            .replace(Regex("(.+)_horse$"), "horse_$1")
            .replace("magma_cube", "magmacube")
            .replace("ender_dragon", "enderdragon")
            .replace("elder_guardian", "guardian")
            .replace("giant", "zombie")
            .replace("polar_bear", "bear")
    }

    /** エンティティ→色の対応表を生成する */
    fun generateEntityToColor() {
        // テクスチャ→色の対応表を読み込む
        val textureToColor =
            Gson().fromJson<Map<String, String>>(FileReader("../block_colors/texture_to_color.json"), Map::class.java)

        // エンティティのテクスチャを取得
        val map = EntityType.values()
            // 未知のエンティティは除外
            .filter { it != EntityType.UNKNOWN }
            .associate { entityType ->
                // エンティティのバニラID
                val key = convertEntityToColor(entityType.key.key)

                // 名前
                val name = entityType.name

                // 対応する色を取得
                val color =
                    // エンティティのテクスチャを含んでいたらその色にする
                    textureToColor.filterKeys { it.startsWith("entity/$key") }.values.firstOrNull()
                        ?: textureToColor.filterKeys { it.contains(key) }.values.firstOrNull()
                        ?: "null"

                // 対応
                println("$name\t${entityType.key.key}\t$color")
                name to color
            }

        // YAMLファイルに保存する
        val yaml = YamlConfiguration()
        map.forEach { (key, value) ->
            yaml.set(key, value)
        }
        yaml.save("../block_colors/entity_to_color.yml")
    }

    /** Materialのサンプルを作成する */
    fun generateMaterialSample(world: World, materialToColor: Configuration) {
        // 配置する高さ
        val height = 4
        Material.values().forEachIndexed { index, material ->
            // ブロックの色をconfigから取得
            val colorName = materialToColor.getString(material.name)
            val color = EnumColor.values().find { it.colorName == colorName }

            // 配置するブロックの座標
            val blockAt = world.getBlockAt((index % 32) * 3, height, -(index / 32) * 5)
            // 床を色付きの羊毛にする
            if (color != null) {
                for (ix in -1..1) {
                    for (iz in -1..1) {
                        blockAt.getRelative(ix, -1, iz).setType(color.wool, false)
                    }
                }
            }
            // ブロックを配置
            if (material.isBlock) {
                // ブロックの場合床に埋め込んで配置
                blockAt.getRelative(0, -1, 0).setType(material, false)
            } else {
                // アイテムの場合ブロックの上に額縁を置いて配置
                world.spawn(blockAt.location, ItemFrame::class.java).apply {
                    setItem(ItemStack(material))
                    setFacingDirection(BlockFace.UP, true)
                    isFixed = true
                }
            }
            // 看板にブロックIDを書いて配置
            blockAt.getRelative(0, 0, -1).apply {
                setType(Material.OAK_SIGN, false)
                val blockState = state as Sign
                material.name.chunked(15).forEachIndexed { i, name ->
                    if (i < 4) {
                        blockState.line(i, Component.text(name))
                    }
                }
                blockState.update()
            }
        }
    }

    /** Materialのサンプルからコンフィグを更新する */
    fun saveMaterialSample(world: World, materialToColor: Configuration) {
        // 配置する高さ
        val height = 4
        Material.values().forEachIndexed { index, material ->
            // 配置するブロックの座標
            val blockAt = world.getBlockAt((index % 32) * 3, height, -(index / 32) * 5)

            // 上に羊毛がある場合はコンフィグを更新
            val newColorBlock = sequence {
                for (ix in -1..1) {
                    for (iz in -1..1) {
                        yield(blockAt.getRelative(ix, 0, iz))
                    }
                }
            }.mapNotNull { block -> EnumColor.values().find { it.wool == block.type } }.firstOrNull()

            if (newColorBlock != null) {
                // 出力
                println("${material.name} = $newColorBlock")
                // ブロックの色をconfigに反映
                materialToColor.set(material.name, newColorBlock.colorName)
            }
        }
    }

    /** EntityTypeのサンプルを作成する */
    fun generateEntitySample(world: World, entityToColor: Configuration) {
        // 配置する高さ
        val height = 4
        EntityType.values()
            // 未知のエンティティは除外
            .filter { it != EntityType.UNKNOWN }
            // プレイヤーは除外
            .filter { it != EntityType.PLAYER }
            .forEachIndexed { index, entityType ->
                // エンティティの色をconfigから取得
                val colorName = entityToColor.getString(entityType.name)
                val color = EnumColor.values().find { it.colorName == colorName }

                // 配置するブロックの座標
                val blockAt = world.getBlockAt((index % 16) * 3, height, 10 + (index / 16) * 5)
                world.spawnEntity(blockAt.location, entityType).apply {
                    setGravity(false)
                    if (this is LivingEntity) {
                        setAI(false)
                        isInvulnerable = true
                        isPersistent = true
                    }
                }
                // 床を色付きの羊毛にする
                if (color != null) {
                    for (ix in -1..1) {
                        for (iz in -1..1) {
                            blockAt.getRelative(ix, -1, iz).setType(color.wool, false)
                        }
                    }
                }
            }
    }

    /** Entityのサンプルからコンフィグを更新する */
    fun saveEntitySample(world: World, entityToColor: Configuration) {
        // 配置する高さ
        val height = 4
        EntityType.values()
            // 未知のエンティティは除外
            .filter { it != EntityType.UNKNOWN }
            // プレイヤーは除外
            .filter { it != EntityType.PLAYER }
            .forEachIndexed { index, entityType ->
                // 配置するブロックの座標
                val blockAt = world.getBlockAt((index % 16) * 3, height, 10 + (index / 16) * 5)
                // 上に羊毛がある場合はコンフィグを更新
                val newColorBlock = sequence {
                    for (ix in -1..1) {
                        for (iz in -1..1) {
                            yield(blockAt.getRelative(ix, 0, iz))
                        }
                    }
                }.mapNotNull { block -> EnumColor.values().find { it.wool == block.type } }.firstOrNull()

                if (newColorBlock != null) {
                    // 出力
                    println("${entityType.name} = $newColorBlock")
                    // エンティティの色をconfigに反映
                    entityToColor.set(entityType.name, newColorBlock.colorName)
                }
            }
    }
}