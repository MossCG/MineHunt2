package net.mcxk.minehunt.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;
/**
public class placeholder extends EZPlaceholderHook{

    private final MineHunt plugin = MineHunt.getInstance();

    public placeholder(Plugin MineHunt){
        super(MineHunt, "Minehunt");
    }
    @Override
    public String onPlaceholderRequest(Player player, String string) {
        Optional<PlayerRole> role = MineHunt.getInstance().getGame().getPlayerRole(player);
        if (string.equals("rule")) {
            if (role.get() == PlayerRole.HUNTER) {
                return plugin.getConfig().getString("HunterName");
            }
            if (role.get() == PlayerRole.RUNNER) {
                return plugin.getConfig().getString("RunnerName");
            }
            if (role.get() == null) {
                return plugin.getConfig().getString("ObserverName");
            }
        }
        return null;
    }
}
    */
public class placeholder extends PlaceholderExpansion {

    private final MineHunt plugin;
    public placeholder(MineHunt plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public boolean canRegister() {
        return true;
    }
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    @Override
    public String getIdentifier() {
        return "Minehunt";
    }
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        Optional<PlayerRole> role = MineHunt.getInstance().getGame().getPlayerRole(player);
        if (identifier.equals("rule")) {
            if (role.get() == PlayerRole.HUNTER) {
                return (ChatColor.RED + plugin.getConfig().getString("HunterName"));
            }
            if (role.get() == PlayerRole.RUNNER) {
                return (ChatColor.GREEN + plugin.getConfig().getString("RunnerName"));
            }
            return (ChatColor.GRAY + plugin.getConfig().getString("ObserverName"));
        }
        return null;
    }
}



