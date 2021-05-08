package net.mcxk.minehunt.watcher;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class PlayerMoveWatcher {
    private boolean runnerNether = false;
    private boolean runnerTheEnd = false;

    public PlayerMoveWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                MineHunt.getInstance().getGame().getInGamePlayers().forEach(player -> {
                    World.Environment environment = player.getWorld().getEnvironment();
                    if (environment != World.Environment.NORMAL) {
                        Optional<PlayerRole> role = MineHunt.getInstance().getGame().getPlayerRole(player);
                        if (role.isPresent()) {
                            if (role.get() == PlayerRole.RUNNER) {
                                if (!runnerNether && environment == World.Environment.NETHER) {
                                    runnerNether = true;
                                    Bukkit.broadcastMessage("逃亡者已到达 下界 维度！");
                                }
                                if (!runnerTheEnd && environment == World.Environment.THE_END) {
                                    runnerTheEnd = true;
                                    Bukkit.broadcastMessage("逃亡者已到达 末地 维度！");
                                }
                            }
                        }
                    }

                });
            }
        }.runTaskTimer(MineHunt.getInstance(), 0, 80);
    }
}
