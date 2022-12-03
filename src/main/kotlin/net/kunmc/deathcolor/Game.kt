package net.kunmc.deathcolor

import com.destroystokyo.paper.block.TargetBlockInfo
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.KeyedBossBar
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * ゲーム進行に関する処理
 */
class Game : Listener {
    /** 死の色に触ったときのダメージ量 */
    private val damageAmount = DeathColor.instance.config.getDouble("game.damage")

    /** プレイ中かどうか */
    private var state: GameState? = null

    /** ボスバーのキー */
    private val bossBarKey = NamespacedKey(DeathColor.instance, "death_color")

    /** ボスバー */
    private val bossBar: KeyedBossBar = (Bukkit.getBossBar(bossBarKey)
        ?: Bukkit.createBossBar(
            bossBarKey,
            "触れたら死ぬ色",
            BarColor.YELLOW,
            BarStyle.SEGMENTED_12
        )).apply { isVisible = false }

    /** 死亡時メッセージ */
    private val deathMessages = mutableMapOf<Player, Component>()

    /** ゲーム開始 */
    fun start() {
        // 開始
        state = GameState(bossBar)
    }

    /** ゲーム終了 */
    fun stop() {
        // 終了
        bossBar.isVisible = false
        state = null
    }

    /** 毎秒呼ばれる、カウントダウンなどのゲーム進行を行う */
    fun tickState() {
        val state = state ?: return

        // ボスバーに全員追加
        Bukkit.getOnlinePlayers().forEach { bossBar.addPlayer(it) }

        // ゲームを進行する
        if (state.playGame.hasNext()) {
            state.playGame.next()
        }

        // インベントリ内のアイテム全てに名前を付ける
        Bukkit.getOnlinePlayers().forEach { player ->
            player.inventory.forEach { item ->
                item?.itemMeta = item.itemMeta?.apply {
                    // アイテムの名前がついていたら無視
                    if (hasDisplayName()) return@apply

                    // ブロックの色をconfigから取得
                    val color = item.type.toEnumColor()

                    // アイテムの名前を変える
                    displayName(
                        Component.translatable(item.translationKey)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text(" ( "))
                            .append(Component.text(color.colorText))
                            .append(Component.text(" ) "))
                    )
                }
            }
        }
    }

    /** 1/4秒ごとに呼ばれる、乗っているブロックやコリジョンを判定する */
    fun tickMove() {
        val state = state ?: return

        for (player in Bukkit.getOnlinePlayers()) {
            // 近くのエンティティを取得
            val nearbyEntities = player.world.getNearbyEntities(player.boundingBox) { entity ->
                entity.type != EntityType.PLAYER
            }

            // 足元のブロックの色を取得
            val footBlock = player.location.clone().add(0.0, -0.3, 0.0).block
            val footColor = footBlock.type.toEnumColor()

            // 目線のブロック/エンティティの色を取得
            val eyeColor = player.getTargetEntity(5, true)?.toEnumColor()
                ?: player.getTargetBlock(5, TargetBlockInfo.FluidMode.ALWAYS)?.type?.toEnumColor()

            // 表示
            player.sendActionBar(
                Component.text("目線: ")
                    .append(Component.text(eyeColor.colorText))
                    .append(Component.text("  足元: "))
                    .append(Component.text(footColor.colorText))
                    .let { component ->
                        val nearby = nearbyEntities.firstNotNullOfOrNull { it.toEnumColor() }
                        if (nearby != null) {
                            component.append(Component.text("  Mob: "))
                                .append(Component.text(nearby.colorText))
                        } else {
                            component
                        }
                    }
            )

            // プレイ中でないなら無視
            if (state.phase != GameState.Phase.PLAYING) continue

            if (footColor == state.deathColor) {
                // 足元が死ぬ色だったら死ぬ
                player.damage(damageAmount) {
                    player.deathMessage(state.deathColor, footBlock.type.text, "を踏んでしまった")
                }
                continue
            }

            val mainHandItem = player.inventory.itemInMainHand.type
            val mainHandColor = mainHandItem.toEnumColor()
            if (mainHandColor == state.deathColor) {
                // メインハンドが死ぬ色だったら死ぬ
                player.damage(damageAmount) {
                    player.deathMessage(state.deathColor, mainHandItem.text, "を右手に持ってしまった")
                }
                continue
            }

            val offHandItem = player.inventory.itemInOffHand.type
            val offHandColor = offHandItem.toEnumColor()
            if (offHandColor == state.deathColor) {
                // オフハンドが死ぬ色だったら死ぬ
                player.damage(damageAmount) {
                    player.deathMessage(state.deathColor, offHandItem.text, "を左手に持ってしまった")
                }
                continue
            }

            val nearbyEntity = nearbyEntities.find { it.toEnumColor() == state.deathColor }
            if (nearbyEntity != null) {
                // 近くに死ぬ色のエンティティがいたら死ぬ
                player.damage(damageAmount) {
                    player.deathMessage(state.deathColor, nearbyEntity.text, "にぶつかってしまった")
                }
            }
        }

        state.deathColor
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // プレイ中でないなら無視
        val state = state ?: return
        if (state.phase != GameState.Phase.PLAYING) return

        // ブロックの色を取得
        val clickedBlock = event.clickedBlock?.type
        val clickedColor = clickedBlock?.toEnumColor() ?: return

        // ブロックの色が死ぬ色だったら
        if (clickedColor == state.deathColor) {
            // 死ぬ
            event.player.damage(damageAmount) {
                event.player.deathMessage(
                    state.deathColor,
                    clickedBlock.text,
                    if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
                        "を殴ってしまった"
                    } else {
                        "を押してしまった"
                    }
                )
            }
        }
    }

    @EventHandler
    fun onPlayerAttackEntity(event: EntityDamageByEntityEvent) {
        // プレイ中でないなら無視
        val state = state ?: return
        if (state.phase != GameState.Phase.PLAYING) return

        // プレイヤーか
        val player = event.damager as? Player ?: return

        // エンティティの色を取得
        val entity = event.entity
        val entityColor = entity.toEnumColor() ?: return

        // エンティティの色が死ぬ色だったら
        if (entityColor == state.deathColor) {
            // 死ぬ
            player.damage(damageAmount) {
                player.deathMessage(state.deathColor, entity.text, "を殴ってしまった")
            }
        }
    }

    private fun Player.deathMessage(
        color: EnumColor,
        text: Component,
        action: String,
    ): TextComponent {
        return Component.text("")
            .append(displayName())
            .append(Component.text("は"))
            .append(Component.text("${color.colorText}色"))
            .append(Component.text("の"))
            .append(text.color(NamedTextColor.GREEN))
            .append(
                Component.text("${action}！")
            )
    }

    /** ダメージ */
    private fun Player.damage(damage: Double, reason: () -> Component) {
        // 死んでいたらダメージを与えない
        if (isDead) return

        // 死んだらメッセージを保存
        if (health - damage <= 0) {
            // 死亡時メッセージを保存
            deathMessages[this] = reason()
        }

        // ダメージを与える
        damage(damage)
    }

    /** 死んだとき */
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        // 死亡時メッセージを取得
        val reason = deathMessages[event.entity] ?: return

        // 死亡時メッセージを表示
        event.deathMessage(reason)

        // 死亡時メッセージを削除
        deathMessages.remove(event.entity)
    }
}