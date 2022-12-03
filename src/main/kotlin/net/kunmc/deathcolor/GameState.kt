package net.kunmc.deathcolor

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BossBar

/** ゲームのステート */
class GameState(private val bossBar: BossBar) {
    /** カウントダウン */
    private var countDown = 0

    /** 今の死の色が続く時間 */
    private var timeGameRemaining = 1

    /** クールダウンタイム */
    private val timeCooldown = DeathColor.instance.config.getInt("game.cooldownTime")

    /** インターバルタイム */
    private val timeInterval = DeathColor.instance.config.getInt("game.intervalTime")

    /** 死の色に触ったときのダメージ量 */
    val timeRespawnCooldown = DeathColor.instance.config.getInt("game.respawnCooldownTime")

    /** 死の色に触ったときのダメージ量 */
    val damageAmount = DeathColor.instance.config.getDouble("game.damage")

    /** 残り時間候補 */
    private val remainingTimeCandidate: List<Int> =
        DeathColor.instance.config.getIntegerList("game.remainingTimeCandidates")

    /** 次の色 */
    var nextColor: EnumColor? = null

    /** ゲームのフェーズ */
    enum class Phase {
        /** 試合前カウントダウン */
        COOLDOWN,

        /** ゲーム中 */
        PLAYING,

        /** インターバル中 */
        INTERVAL,
    }

    /** 現在のフェーズ */
    var phase = Phase.COOLDOWN

    /** 死ぬ色 */
    var deathColor: EnumColor = EnumColor.WHITE
        private set


    /** ランダムな色にセットする */
    private fun setRandomDeathColor() {
        deathColor = nextColor
            ?: EnumColor.values().random()
        nextColor = null
    }

    /** ランダムな残り時間にセット */
    private fun setRandomCountDown() {
        // コンフィグからカウントダウンの秒数候補を取得
        val countDownList = remainingTimeCandidate
        // ランダムに選択
        timeGameRemaining = countDownList.random()
        countDown = timeGameRemaining
    }

    /** ボスバーを表示する */
    private fun showBossBar() {
        // ボスバーを表示
        bossBar.isVisible = true

        // ボスバーを更新
        when (phase) {
            Phase.COOLDOWN -> {
                bossBar.color = BarColor.BLUE
                bossBar.progress = (countDown.toDouble() / timeCooldown).coerceIn(0.0..1.0)
                bossBar.setTitle("${ChatColor.BOLD}ゲーム開始まで${ChatColor.YELLOW} ${countDown.minuteSecondString}")
            }

            Phase.PLAYING -> {
                bossBar.color = deathColor.barColor
                bossBar.progress = (countDown.toDouble() / timeGameRemaining).coerceIn(0.0..1.0)
                bossBar.setTitle("${ChatColor.WHITE}「 ${deathColor.chatColor}${ChatColor.BOLD}${deathColor.langName}${ChatColor.WHITE} 」")
            }

            Phase.INTERVAL -> {
                bossBar.color = BarColor.YELLOW
                bossBar.progress = (countDown.toDouble() / timeInterval).coerceIn(0.0..1.0)
                bossBar.setTitle("${ChatColor.BOLD}休憩時間")
            }
        }
    }

    /** 触れてはいけない色をタイトルで表示する */
    private fun showCooldownTitle() {
        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(
                "${ChatColor.WHITE}「 ${deathColor.chatColor}${ChatColor.BOLD}${deathColor.langName}${ChatColor.WHITE} 」",
                "に触れるな！",
                10,
                90,
                0
            )
        }
        Bukkit.broadcastMessage("$CHAT_PREFIX 次の触れたら死ぬ色は「 ${deathColor.chatColor}${ChatColor.BOLD}${deathColor.langName}${ChatColor.WHITE} 」です")
        Bukkit.broadcastMessage("$CHAT_PREFIX  ${timeCooldown.minuteSecondString}後 に開始します")
    }

    /** カウントダウンタイトルを表示 */
    private fun showCountDownTitle() {
        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(
                "${ChatColor.YELLOW}${ChatColor.BOLD}${countDown}",
                "開始まで",
                0,
                30,
                10
            )
        }
    }

    /** スタート時のタイトルを表示 */
    private fun showStartTitle() {
        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(
                "${ChatColor.GREEN}${ChatColor.BOLD}スタート",
                "${ChatColor.WHITE}今回の色は「 ${deathColor.chatColor}${ChatColor.BOLD}${deathColor.langName}${ChatColor.WHITE} 」",
                0,
                40,
                10
            )
        }
        Bukkit.broadcastMessage("$CHAT_PREFIX スタート")
        Bukkit.broadcastMessage("$CHAT_PREFIX  触れたら死ぬ色は「 ${deathColor.chatColor}${ChatColor.BOLD}${deathColor.langName}${ChatColor.WHITE} 」です")
        Bukkit.broadcastMessage(
            "$CHAT_PREFIX  制限時間は ${ChatColor.YELLOW}${timeGameRemaining.minuteSecondString}${ChatColor.RESET} です"
        )
    }

    /** インターバルチャットを表示 */
    private fun showIntervalChat() {
        Bukkit.broadcastMessage(
            "$CHAT_PREFIX フェーズクリア！ " +
                    "これより ${timeInterval.minuteSecondString} の休憩時間を開始します"
        )
    }

    /** カウントダウン音を鳴らす */
    private fun playCountDownSound() {
        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f)
        }
    }

    /** ゲーム開始音を鳴らす */
    private fun playStartSound() {
        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f)
        }
    }

    /** ゲーム終了音を鳴らす */
    private fun playFinishSound() {
        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
        }
    }


    /** ゲーム進行 */
    val playGame = sequence {
        while (true) {
            // お題設定
            setRandomDeathColor()
            // お題表示
            showCooldownTitle()
            // タイトルが消えるまで待つ
            for (i in 0 until 4) yield(true)

            // 開始前カウントダウン開始
            phase = Phase.COOLDOWN
            countDown = timeCooldown
            // 開始前カウントダウン
            while (countDown >= 0) {
                showCountDownTitle()
                showBossBar()
                playCountDownSound()
                countDown--
                yield(true)
            }

            // ゲーム開始
            phase = Phase.PLAYING
            playStartSound()
            setRandomCountDown()
            showBossBar()
            showStartTitle()
            while (countDown >= 0) {
                showBossBar()
                if (countDown < 5) playCountDownSound()
                countDown--
                yield(true)
            }

            // インターバル
            playFinishSound()
            showIntervalChat()
            phase = Phase.INTERVAL
            countDown = timeInterval
            // インターバルカウントダウン
            while (countDown >= 0) {
                showBossBar()
                countDown--
                yield(true)
            }
        }
    }.iterator()
}