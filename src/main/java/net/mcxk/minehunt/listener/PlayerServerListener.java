package net.mcxk.minehunt.listener;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class PlayerServerListener implements Listener {
    private final MineHunt plugin = MineHunt.getInstance();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void join(PlayerJoinEvent event) {
        if (plugin.getGame().getStatus() == GameStatus.WAITING_PLAYERS) {
            if (plugin.getGame().playerJoining(event.getPlayer())) {
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
            } else {
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().sendMessage("当前游戏已满人，您现在处于观战状态");
            }
        } else {
            //处理玩家重连
            if (plugin.getGame().getInGamePlayers().stream().anyMatch(p -> p.getUniqueId().equals(event.getPlayer().getUniqueId()))) {
                plugin.getGame().getInGamePlayers().removeIf(p -> p.getUniqueId().equals(event.getPlayer().getUniqueId()));
                plugin.getGame().getInGamePlayers().add(event.getPlayer());

                for (Map.Entry<Player, PlayerRole> playerPlayerRoleEntry : plugin.getGame().getRoleMapping().entrySet()) {
                    if (playerPlayerRoleEntry.getKey().getUniqueId().equals(event.getPlayer().getUniqueId())) {
                        PlayerRole role = playerPlayerRoleEntry.getValue();
                        plugin.getGame().getRoleMapping().remove(playerPlayerRoleEntry.getKey());
                        plugin.getGame().getRoleMapping().put(event.getPlayer(), role);
                        break;
                    }
                }

                if (plugin.getGame().getInGamePlayers().contains(event.getPlayer())) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "玩家 " + event.getPlayer().getName() + " 已重新连接");
                    plugin.getGame().getReconnectTimer().entrySet().removeIf(set -> set.getKey().getUniqueId().equals(event.getPlayer().getUniqueId()));
                }

            } else {
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().sendMessage("游戏已经开始，您现在处于观战状态");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event) {
        if (!plugin.getGame().getInGamePlayers().contains(event.getPlayer())) {
            return;
        }
        plugin.getGame().playerLeaving(event.getPlayer());
    }

}
