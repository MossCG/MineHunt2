package net.mcxk.minehunt.listener;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class PlayerInteractListener implements Listener {
    private final MineHunt plugin = MineHunt.getInstance();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void clickXJB(PlayerInteractEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void damageXJB(EntityDamageEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void runXJB(FoodLevelChangeEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void teamDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player1 = (Player) event.getEntity();
        Player player2 = (Player) event.getDamager();

        Optional<PlayerRole> player1Role = plugin.getGame().getPlayerRole(player1);
        Optional<PlayerRole> player2Role = plugin.getGame().getPlayerRole(player2);
        if (player1Role.isPresent() && player2Role.isPresent()) {
            if (player1Role.get() == player2Role.get()) {
                double historyDamage = plugin.getGame().getTeamDamageData().getOrDefault(player2, 0.0d);
                historyDamage += event.getFinalDamage();
                plugin.getGame().getTeamDamageData().put(player2, historyDamage);
            }
        }
    }


}
