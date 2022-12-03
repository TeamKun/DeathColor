package net.kunmc.deathcolor

import com.google.gson.Gson
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
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
            .filter { it != EntityType.UNKNOWN && it.isAlive }
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
}