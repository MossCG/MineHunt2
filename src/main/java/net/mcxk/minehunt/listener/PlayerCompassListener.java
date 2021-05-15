package net.mcxk.minehunt.listener;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.List;
import java.util.Optional;

public class PlayerCompassListener implements Listener {
    private final MineHunt plugin = MineHunt.getInstance();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void craftCompass(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() != Material.COMPASS) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Optional<PlayerRole> role = plugin.getGame().getPlayerRole(player);
        if (!role.isPresent()) {
            return;
        }
        if (role.get() == PlayerRole.HUNTER) {
            plugin.getGame().switchCompass(true); //猎人合成，解锁
        } else if (role.get() == PlayerRole.RUNNER) {
            plugin.getGame().switchCompass(false); //逃亡者合成，锁回去
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void respawnGivenCompass(PlayerRespawnEvent event) {
        if (plugin.getGame().getStatus() == GameStatus.GAME_STARTED && plugin.getGame().isCompassUnlocked()) {
            Optional<PlayerRole> role = plugin.getGame().getPlayerRole(event.getPlayer());
            if (role.isPresent()) {
                if (role.get() == PlayerRole.HUNTER) {
                    event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void deathDropRemoveCompass(PlayerDeathEvent event) {
        event.getDrops().removeIf(itemStack -> itemStack.getType() == Material.COMPASS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void clickCompass(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (event.getItem() == null || event.getItem().getType() != Material.COMPASS) {
            return;
        }
        if (!plugin.getGame().isCompassUnlocked()) {
            event.getPlayer().setCompassTarget(event.getPlayer().getWorld().getSpawnLocation());
            event.getPlayer().sendMessage("你的队伍还没有解锁指南针，请先合成一个来解锁。");
        }
        List<Player> runners = plugin.getGame().getPlayersAsRole(PlayerRole.RUNNER);
        if (runners.isEmpty()) {
            event.getPlayer().sendMessage("追踪失败，所有逃亡者均已离线等待重连中...");
        }
        Player closestRunner = null;
        for (Player runner : plugin.getGame().getPlayersAsRole(PlayerRole.RUNNER)) {
            if (runner.getWorld() != event.getPlayer().getWorld()) {
                continue;
            }
            if(runner.getGameMode() == GameMode.SPECTATOR){
                continue;
            }
            if (closestRunner == null) {
                closestRunner = runner;
                continue;
            }
            if (event.getPlayer().getLocation().distance(runner.getLocation()) < closestRunner.getLocation().distance(event.getPlayer().getLocation())) {
                closestRunner = runner;
            }
        }
        if (closestRunner == null) {
            event.getPlayer().sendMessage("追踪失败，没有任何逃亡者和您所处的世界相同");
        } else {
            TextComponent component = new TextComponent("成功探测到距离您最近的逃亡者！正在追踪: %s".replace("%s", closestRunner.getName()));
            component.setColor(ChatColor.AQUA);
            if(event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL){
                event.getPlayer().setCompassTarget(closestRunner.getLocation());
            }else{
                CompassMeta compassMeta = (CompassMeta)event.getItem().getItemMeta();
                if(compassMeta == null){
                    event.getPlayer().sendMessage("错误：指南针损坏，请联系服务器管理员报告BUG.");
                }
                compassMeta.setLodestone(closestRunner.getLocation());
                compassMeta.setLodestoneTracked(false); //如果为true，则目标位置必须有Lodestone才有效；因此设为false 这貌似也是ManiHunt中的一个BUG
                event.getItem().setItemMeta(compassMeta);
            }event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
        }
    }
}
