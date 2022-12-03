package net.kunmc.deathcolor

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.KeyedBossBar
import org.bukkit.event.Listener

/**
 * ゲーム進行に関する処理
 */
class Game : Listener {
    /** プレイ中かどうか */
    var state: GameState? = null

    /** ボスバーのキー */
    private val bossBarKey = NamespacedKey(DeathColor.instance, "death_color")

    /** ボスバー */
    val bossBar: KeyedBossBar = (Bukkit.getBossBar(bossBarKey)
        ?: Bukkit.createBossBar(
            bossBarKey,
            "触れたら死ぬ色",
            BarColor.YELLOW,
            BarStyle.SEGMENTED_12
        )).apply { isVisible = false }

    fun start() {
        // 開始
        state = GameState(bossBar)
    }

    fun stop() {
        // 終了
        bossBar.isVisible = false
        state = null
    }

    fun tick() {
        val state = state ?: return

        // ボスバーに全員追加
        Bukkit.getOnlinePlayers().forEach { bossBar.addPlayer(it) }

        // ゲームを進行する
        if (state.playGame.hasNext()) {
            state.playGame.next()
        }
    }
}