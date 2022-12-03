package net.kunmc.deathcolor

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class DeathColor : JavaPlugin() {
    /** Material→色の対応表 */
    lateinit var materialToColor: YamlConfiguration

    /** EntityType→色の対応表 */
    lateinit var entityToColor: YamlConfiguration

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        materialToColor = saveCustomConfig("material_to_color.yml")
        entityToColor = saveCustomConfig("entity_to_color.yml")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    /** コンフィグファイルを読み込む、なければresourcesからコピーする */
    private fun saveCustomConfig(name: String): YamlConfiguration {
        val file = File(dataFolder, name)
        if (!file.exists()) {
            saveResource(name, false)
        }
        return YamlConfiguration.loadConfiguration(file)
    }

    /** コマンド */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name != "deathcolor") return false

        // 対応表をワールドに配置して調整できるコマンド
        if (args.getOrNull(0) == "debug_generate_material_to_color") {
            // 配置するワールド、オーバーワールドの中心に配置
            val world = sender.server.worlds.first()

            when (args.getOrNull(1)) {
                "generateMaterial" -> {
                    // ブロック→色の対応表を生成
                    ColorMapGenerator.generateBlockToColor()
                    sender.sendMessage("ブロック→色の対応表を生成しました")
                }

                "generateEntity" -> {
                    // エンティティ→色の対応表を生成
                    ColorMapGenerator.generateEntityToColor()
                    sender.sendMessage("エンティティ→色の対応表を生成しました")
                }

                "material" -> {
                    sender.sendMessage("ブロック→色のサンプルを生成します")
                    ColorMapGenerator.generateMaterialSample(world, materialToColor)
                    sender.sendMessage("ブロック→色のサンプルを生成しました")
                }

                "entity" -> {
                    sender.sendMessage("エンティティ→色のサンプルを生成します")
                    ColorMapGenerator.generateEntitySample(world, entityToColor)
                    sender.sendMessage("エンティティ→色のサンプルを生成しました")
                }

                "saveMaterial" -> {
                    // ブロック→色の対応表を保存
                    ColorMapGenerator.saveMaterialSample(world, materialToColor)
                    // コンフィグに保存
                    materialToColor.save(File(dataFolder, "material_to_color.yml"))
                    sender.sendMessage("ブロック→色の対応表を保存しました")
                }

                "saveEntity" -> {
                    // エンティティ→色の対応表を保存
                    ColorMapGenerator.saveEntitySample(world, entityToColor)
                    // コンフィグに保存
                    entityToColor.save(File(dataFolder, "entity_to_color.yml"))
                    sender.sendMessage("エンティティ→色の対応表を保存しました")
                }

                else -> return false
            }
            return true
        }

        return false
    }

}