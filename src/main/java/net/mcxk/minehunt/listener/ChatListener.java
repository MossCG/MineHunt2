package net.mcxk.minehunt.listener;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;


public class ChatListener implements Listener {
    private final MineHunt plugin = MineHunt.getInstance();
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent event) {
        if (MineHunt.getInstance().getGame().getStatus() != GameStatus.GAME_STARTED) {
            return;
        }
        Optional<PlayerRole> role = MineHunt.getInstance().getGame().getPlayerRole(event.getPlayer());
        if (!role.isPresent()) {
            event.setFormat(ChatColor.GRAY + plugin.getConfig().getString("ObserverName") + " " + event.getPlayer().getDisplayName() + " " + event.getMessage());
            return;
        }

        if (event.getMessage().startsWith("#")) {
            event.setCancelled(true);
            if (role.get() == PlayerRole.HUNTER) {
                MineHunt.getInstance().getGame().getPlayersAsRole(PlayerRole.HUNTER).forEach(p -> p.sendMessage(ChatColor.GRAY + "[TEAM] " + event.getPlayer().getDisplayName() + ": " + ChatColor.RESET + event.getMessage()));
            }
        } else {
            if (role.get() == PlayerRole.HUNTER) {
                event.setFormat(ChatColor.RED + plugin.getConfig().getString("HunterName") + " " + event.getPlayer().getDisplayName() + " " + ChatColor.RESET + event.getMessage());
            } else if (role.get() == PlayerRole.RUNNER) {
                event.setFormat(ChatColor.GREEN + plugin.getConfig().getString("RunnerName") + " " + event.getPlayer().getDisplayName() + " " + ChatColor.RESET + event.getMessage());
            }
        }
    }
}
