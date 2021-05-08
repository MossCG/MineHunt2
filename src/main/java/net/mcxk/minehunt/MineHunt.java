package net.mcxk.minehunt;

import lombok.Getter;
import net.mcxk.minehunt.game.Game;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import net.mcxk.minehunt.listener.*;
import net.mcxk.minehunt.watcher.CountDownWatcher;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class MineHunt extends JavaPlugin {
    @Getter
    private static MineHunt instance;
    @Getter
    private Game game;

    @Getter
    private CountDownWatcher countDownWatcher;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        instance = this;
        game = new Game();
        countDownWatcher = new CountDownWatcher();
        game.switchWorldRuleForReady(false);
        Bukkit.getPluginManager().registerEvents(new PlayerServerListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerCompassListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProgressDetectingListener(), this);
        Bukkit.getPluginManager().registerEvents(new GameWinnerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return false;
        }

        //禁止删除本行版权声明
        //墨守吐槽：如果有人想在我这搞分支就顺着往下写就好了~
        if (args[0].equalsIgnoreCase("copyright")) {
            sender.sendMessage("Copyright - Minecraft of gamerteam. 版权所有.");
			sender.sendMessage("Fork by MossCG 这是墨守的分支版本~");
            return true;
        }


        if (!sender.hasPermission("minehunt.admin")) {
            return false;
        }

        //不安全命令 完全没做检查，确认你会用再执行
        //墨守吐槽：挺安全的起码我没用出啥问题，有空我改改2333
        if (args[0].equalsIgnoreCase("hunter") || args[0].equalsIgnoreCase("runner")) {
            Player player = (Player) sender;
            this.getGame().getInGamePlayers().add(player);
            if (args[0].equalsIgnoreCase("hunter")) {
                this.getGame().getRoleMapping().put(player, PlayerRole.HUNTER);
            } else {
                this.getGame().getRoleMapping().put(player, PlayerRole.RUNNER);
            }
            player.setGameMode(GameMode.SURVIVAL);
            Bukkit.broadcastMessage("玩家 " + sender.getName() + " 强制加入了游戏！ 身份：" + args[0]);
            return true;
        }
        if (args[0].equalsIgnoreCase("resetcountdown") && this.getGame().getStatus() == GameStatus.WAITING_PLAYERS) {
            this.getCountDownWatcher().resetCountdown();
            return true;
        }
        if (args[0].equalsIgnoreCase("forcestart") && this.getGame().getStatus() == GameStatus.WAITING_PLAYERS) {
            if (this.getGame().getInGamePlayers().size() < 2) {
                sender.sendMessage("错误：至少有2名玩家才可以强制开始游戏 1名玩家你玩个锤子");
                return true;
            }
            return true;
        }

        return false;
    }

}
