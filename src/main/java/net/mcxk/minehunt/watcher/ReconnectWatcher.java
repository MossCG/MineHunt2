package net.mcxk.minehunt.watcher;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ReconnectWatcher {
    private final MineHunt plugin = MineHunt.getInstance();

    public ReconnectWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
                    return;
                }
                List<Player> removing = new ArrayList<>();
                plugin.getGame().getReconnectTimer().forEach((key, value) -> {
                    if (System.currentTimeMillis() - value > 1000 * 600) {
                        removing.add(key);
                    }
                });
                //Remove timeout players from the their team.
                removing.forEach(player -> {
                    plugin.getGame().getReconnectTimer().remove(player);
                    if (player.isOnline()) {
                        return;
                    }
                    plugin.getGame().playerLeft(player);
                });
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}
