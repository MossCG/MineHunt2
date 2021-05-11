package net.mcxk.minehunt.watcher;

import lombok.Getter;
import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class RadarWatcher {
    private final MineHunt plugin = MineHunt.getInstance();
    @Getter
    private final int warnDistance = plugin.getConfig().getInt("WarnDistance");
    public RadarWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
                    return;
                }
                List<Player> runners = plugin.getGame().getPlayersAsRole(PlayerRole.RUNNER);
                List<Player> hunters = plugin.getGame().getPlayersAsRole(PlayerRole.HUNTER);
                for (Player hunter : hunters) {
                    for (Player runner : runners) {
                        if (hunter.getWorld() != runner.getWorld()) {
                            continue;
                        }
                        if(runner.getGameMode() == GameMode.SPECTATOR){
                            continue;
                        }
                        double distance = hunter.getLocation().distance(runner.getLocation());
                        TextComponent textComponent;
                        if (distance > warnDistance) {
                            textComponent = new TextComponent(plugin.getConfig().getString("WarnDistanceSafe").replace("%wd", String.valueOf(warnDistance)));
                            textComponent.setColor(ChatColor.GREEN);
                        } else {
                            textComponent = new TextComponent(plugin.getConfig().getString("WarnDistanceClose").replace("%d", String.valueOf((int) distance)));
                            textComponent.setColor(ChatColor.RED);
                        }
                        runner.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
                    }
                }
            }
        }.runTaskTimer(MineHunt.getInstance(), 0, 5);
    }
}
