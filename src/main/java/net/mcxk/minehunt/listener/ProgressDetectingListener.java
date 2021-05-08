package net.mcxk.minehunt.listener;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameProgress;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ProgressDetectingListener implements Listener {
    private final MineHunt plugin = MineHunt.getInstance();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void craftCompass(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() != Material.COMPASS) {
            return;
        }
        plugin.getGame().getProgressManager().unlockProgress(GameProgress.COMPASS_UNLOCKED);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void getIron(FurnaceExtractEvent event) {
        if (event.getItemType() != Material.IRON_INGOT) {
            return;
        }
        plugin.getGame().getProgressManager().unlockProgress(GameProgress.IRON_MINED);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void breakStone(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.STONE) {
            return;
        }
        plugin.getGame().getProgressManager().unlockProgress(GameProgress.STONE_AGE);
        plugin.getGame().getGameEndingData().setStoneAgePassed(event.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void changeDim(PlayerPortalEvent event) {
        if (event.getTo().getWorld().getEnvironment() == World.Environment.NETHER) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.ENTER_NETHER);
            return;
        }
        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.ENTER_END);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void teleport(PlayerTeleportEvent event) {
        if (event.getTo().getWorld().getEnvironment() == World.Environment.NETHER) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.ENTER_NETHER);
            return;
        }
        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.ENTER_END);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void pickup(EntityPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.ENDER_PEARL) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.GET_ENDER_PERAL);
            return;
        }
        if (event.getItem().getItemStack().getType() == Material.BLAZE_ROD) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.GET_BLAZE_ROD);
        }
    }


}
