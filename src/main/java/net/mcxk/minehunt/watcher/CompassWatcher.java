package net.mcxk.minehunt.watcher;

import lombok.Getter;
import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CompassWatcher {
    @Getter
    private final MineHunt plugin = MineHunt.getInstance();

/**    public CompassWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
                    return;
                }
                List<Player> runners = plugin.getGame().getPlayersAsRole(PlayerRole.RUNNER);
                List<Player> hunters = plugin.getGame().getPlayersAsRole(PlayerRole.HUNTER);
                for (Player runner : runners) {
                    for (Player hunter : hunters) {
                        if (hunter.getWorld() != runner.getWorld()) {
                            continue;
                        }
                        if (!plugin.getGame().isCompassUnlocked()) {
                            hunter.getPlayer().setCompassTarget(hunter.getPlayer().getWorld().getSpawnLocation());
                            hunter.getPlayer().sendMessage("你的队伍还没有解锁指南针，请先合成一个来解锁。");
                        }
                        if (runners.isEmpty()) {
                            hunter.getPlayer().sendMessage("追踪失败，所有逃亡者均已离线等待重连中...");
                        }

                        Player closestRunner = null;
                        if (runner.getWorld() != runner.getPlayer().getWorld()) {
                            continue;
                        }
                        if(runner.getGameMode() == GameMode.SPECTATOR){
                            continue;
                        }
                        if (closestRunner == null) {
                            closestRunner = runner;
                            continue;
                        }
                        if (runner.getPlayer().getLocation().distance(runner.getLocation()) < closestRunner.getLocation().distance(runner.getPlayer().getLocation())) {
                            closestRunner = runner;
                        }
                        if (closestRunner == null) {
                            hunter.getPlayer().sendMessage("追踪失败，没有任何逃亡者和您所处的世界相同");
                            hunter.spigot().sendMessage(ChatMessageType.ACTION_BAR);
                        } else {
                            TextComponent component = new TextComponent("成功探测到距离您最近的逃亡者！正在追踪: %s".replace("%s", closestRunner.getName()));
                            component.setColor(ChatColor.AQUA);
                            if (runner.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL) {
                                hunter.getPlayer().setCompassTarget(closestRunner.getLocation());
                            }
                            hunter.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                        }
                    }
                }
            }
        }.runTaskTimer(MineHunt.getInstance(), 0, 5);
    }*/
}