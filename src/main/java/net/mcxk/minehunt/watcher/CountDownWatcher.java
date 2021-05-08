package net.mcxk.minehunt.watcher;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.Game;
import net.mcxk.minehunt.game.GameStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class CountDownWatcher {
    int remains = MineHunt.getInstance().getGame().getCountdown();
    int shorter = 5;

    public CountDownWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Game game = MineHunt.getInstance().getGame();
                if (game.getStatus() != GameStatus.WAITING_PLAYERS) {
                    return;
                }
                if (remains <= 0) {
                    game.getInGamePlayers().forEach(p ->
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f));
                    game.start();
                    return;
                }
                if (game.getInGamePlayers().size() < game.getMinPlayers()) {
                    game.getInGamePlayers().forEach(p -> p.sendTitle(ChatColor.AQUA + "" + game.getInGamePlayers().size() + " " + ChatColor.WHITE + "/ " + ChatColor.AQUA + game.getMinPlayers(),
                            "正在等待更多玩家加入游戏....", 0, 40, 0));
                    remains = MineHunt.getInstance().getGame().getCountdown();
                    return;
                } else {
                    game.getInGamePlayers().forEach(p -> {
                        p.sendTitle(ChatColor.GOLD.toString() + remains,
                                "游戏即将开始... [" + game.getInGamePlayers().size() + "/" + game.getMaxPlayers() + "]", 0, 40, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 1.0f);
                    });
                }
                remains--;
                if (game.getInGamePlayers().size() >= game.getMaxPlayers()) {
                    if (remains > shorter) {
                        remains = shorter;
                        Bukkit.broadcastMessage("玩家到齐，倒计时缩短！");
                    }
                }
            }
        }.runTaskTimer(MineHunt.getInstance(), 0, 20);
    }

    public void resetCountdown() {
        this.remains = MineHunt.getInstance().getGame().getCountdown();
        Bukkit.broadcastMessage("倒计时已被重置");
    }
}
