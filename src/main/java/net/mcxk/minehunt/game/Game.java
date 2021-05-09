package net.mcxk.minehunt.game;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.replay.GameRecord;
import net.mcxk.minehunt.util.GameEndingData;
import net.mcxk.minehunt.util.MusicPlayer;
import net.mcxk.minehunt.util.StatisticsBaker;
import net.mcxk.minehunt.util.Util;
import net.mcxk.minehunt.watcher.PlayerMoveWatcher;
import net.mcxk.minehunt.watcher.RadarWatcher;
import net.mcxk.minehunt.watcher.ReconnectWatcher;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Game {
    @Getter
    final Map<Player, Double> teamDamageData = new HashMap<>();
    private final MineHunt plugin = MineHunt.getInstance();
    @Getter
    private final Set<Player> inGamePlayers = Sets.newCopyOnWriteArraySet(); //线程安全
    @Getter
    private final int countdown = 30;
    @Getter
    private final Map<Player, Long> reconnectTimer = new HashMap<>();
    @Getter
    private final GameProgressManager progressManager = new GameProgressManager();
    @Getter
    private final GameEndingData gameEndingData = new GameEndingData();
    private final Map<World, Difficulty> difficultyMap = new HashMap<>();
    @Getter
    @Setter
    private GameStatus status = GameStatus.WAITING_PLAYERS;
    @Getter
    private Map<Player, PlayerRole> roleMapping; //线程安全
    @Getter
    private final int maxPlayers = plugin.getConfig().getInt("max-players");
    @Getter
    private final int minPlayers = plugin.getConfig().getInt("min-players");
	private final int L0Player = plugin.getConfig().getInt("L0Player");
	private final int L0Runner = plugin.getConfig().getInt("L0Runner");
	private final int L1Player = plugin.getConfig().getInt("L1Player");
	private final int L1Runner = plugin.getConfig().getInt("L1Runner");
	private final int L2Player = plugin.getConfig().getInt("L2Player");
	private final int L2Runner = plugin.getConfig().getInt("L2Runner");
	private final int L3Player = plugin.getConfig().getInt("L3Player");
	private final int L3Runner = plugin.getConfig().getInt("L3Runner");
    private final int XRandom = plugin.getConfig().getInt("XRandom");
    private final int XBasic = plugin.getConfig().getInt("XBasic");
    private final int YRandom = plugin.getConfig().getInt("YRandom");
    private final int YBasic = plugin.getConfig().getInt("YBasic");
    @Getter
    private boolean CompassUnlocked = plugin.getConfig().getBoolean("CompassUnlocked");


    public Game() {
        fixConfig();
    }

    public void switchCompass(boolean unlocked) {
        if (this.CompassUnlocked == unlocked) {
            return;
        }
        this.CompassUnlocked = unlocked;
        if (unlocked) {
            getPlayersAsRole(PlayerRole.HUNTER).forEach(p -> p.getInventory().addItem(new ItemStack(Material.COMPASS, 1)));
            Bukkit.broadcastMessage(ChatColor.YELLOW + "猎人已解锁追踪指南针！逃亡者的位置已经暴露！");
        } else {
            getPlayersAsRole(PlayerRole.HUNTER).forEach(p -> p.getInventory().remove(Material.COMPASS));
            Bukkit.broadcastMessage(ChatColor.YELLOW + "猎人的追踪指南针被破坏失效，需要重新解锁！");
        }
        getPlayersAsRole(PlayerRole.RUNNER).forEach(p -> p.getInventory().remove(Material.COMPASS)); //清除合成的指南针
    }

    /**
     * 获取玩家角色
     *
     * @param player 玩家
     * @return 可能是Empty（玩家不属于游戏中的玩家）否则返回玩家角色
     */
    public Optional<PlayerRole> getPlayerRole(Player player) {
        if (!this.roleMapping.containsKey(player)) {
            return Optional.empty();
        }
        return Optional.of(this.roleMapping.get(player));
    }

    public boolean playerJoining(Player player) {
        reconnectTimer.remove(player);
        if (inGamePlayers.size() < maxPlayers) {
            inGamePlayers.add(player);
            return true;
        }
        return false;
    }

    public void fixConfig() {
    }

    public void playerLeaving(Player player) {
        if (status == GameStatus.WAITING_PLAYERS) {
            this.inGamePlayers.remove(player);
        } else {
            this.reconnectTimer.put(player, System.currentTimeMillis());
        }
    }

    public void playerLeft(Player player) {
        this.roleMapping.remove(player);
        this.inGamePlayers.remove(player);

        if (getPlayersAsRole(PlayerRole.RUNNER).isEmpty() || getPlayersAsRole(PlayerRole.HUNTER).isEmpty()) {
            Bukkit.broadcastMessage("由于比赛的一方所有人因为长时间未能重新连接而被从列表中剔除，游戏被迫终止。");
            Bukkit.broadcastMessage("服务器将会在 10 秒钟后重新启动。");
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.shutdown();
                }
            }.runTaskLater(plugin, 200);
            return;
        }
        Bukkit.broadcastMessage("玩家：" + player.getName() + " 因长时间未能重新连接回对战而被从列表中剔除");
        Bukkit.broadcastMessage(ChatColor.RED + "猎人: " + Util.list2String(getPlayersAsRole(PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList())));
        Bukkit.broadcastMessage(ChatColor.GREEN + "逃亡者: " + Util.list2String(getPlayersAsRole(PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList())));
    }

    public void start() {
        if (status != GameStatus.WAITING_PLAYERS) {
            return;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            Bukkit.broadcastMessage("请稍等，正在启动游戏录制...");
            GameRecord.record(this);
        }
        Bukkit.broadcastMessage("请稍后，系统正在随机分配玩家身份...");
        Random random = new Random();
        List<Player> noRolesPlayers = new ArrayList<>(inGamePlayers);
        Map<Player, PlayerRole> roleMapTemp = new HashMap<>();

        int runners = 1;
		if (inGamePlayers.size() >= L0Player) {
            runners = L0Runner;
        }
		if (inGamePlayers.size() >= L1Player) {
            runners = L1Runner;
        }
		if (inGamePlayers.size() >= L2Player) {
            runners = L2Runner;
        }
		if (inGamePlayers.size() >= L3Player) {
            runners = L3Runner;
        }

        for (int i = 0; i < runners; i++) {
            Player selected = noRolesPlayers.get(random.nextInt(noRolesPlayers.size()));
            roleMapTemp.put(selected, PlayerRole.RUNNER);
            noRolesPlayers.remove(selected);
        }
        noRolesPlayers.forEach(p -> roleMapTemp.put(p, PlayerRole.HUNTER));
        this.roleMapping = new ConcurrentHashMap<>(roleMapTemp);
        Bukkit.broadcastMessage("正在将逃亡者随机传送到远离猎人的位置...");
        Location airDropLoc = airDrop(getPlayersAsRole(PlayerRole.RUNNER).get(0).getWorld().getSpawnLocation());
        getPlayersAsRole(PlayerRole.RUNNER).forEach(runner -> runner.teleport(airDropLoc));
        getPlayersAsRole(PlayerRole.HUNTER).forEach(p -> p.teleport(p.getWorld().getSpawnLocation()));
        Bukkit.broadcastMessage("设置游戏规则...");
        inGamePlayers.forEach(p -> {
            p.setGameMode(GameMode.SURVIVAL);
            p.setFoodLevel(40);
            p.setHealth(p.getMaxHealth());
            p.setExp(0.0f);
            p.setCompassTarget(p.getWorld().getSpawnLocation());
            p.getInventory().clear();
        });
        if (CompassUnlocked == true) {
            getPlayersAsRole(PlayerRole.HUNTER).forEach(p -> p.getInventory().addItem(new ItemStack(Material.COMPASS, 1)));
        }
        switchWorldRuleForReady(true);
        Bukkit.broadcastMessage("游戏开始！");
        Bukkit.broadcastMessage(ChatColor.AQUA + "欢迎来到 " + ChatColor.GREEN + "MineHunt " + ChatColor.AQUA + "!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "在本游戏中，将会有 " + ChatColor.YELLOW + runners + ChatColor.AQUA + " 名玩家扮演逃亡者，其余玩家扮演猎人");
        Bukkit.broadcastMessage(ChatColor.RED + "猎人需要阻止逃亡者击杀末影龙或击杀逃亡者以取得胜利。");
        Bukkit.broadcastMessage(ChatColor.GREEN + "逃亡者需要在猎人的追杀下击败末影龙以取得胜利。逃亡者无法复活且由于任何原因死亡均会导致猎人胜利。");
        Bukkit.broadcastMessage(ChatColor.AQUA + "在游戏过程中，当你解锁特定的游戏阶段时，全体玩家将会获得阶段奖励，可能是特定物品也可能是增益效果。");
        Bukkit.broadcastMessage(ChatColor.AQUA + "猎人可以通过合成指南针来定位逃亡者的方向；逃亡者可以通过合成指南针摧毁猎人的指南针。");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "祝君好运，末地见！");
        Bukkit.broadcastMessage(ChatColor.RED + "猎人: " + Util.list2String(getPlayersAsRole(PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList())));
        Bukkit.broadcastMessage(ChatColor.GREEN + "逃亡者: " + Util.list2String(getPlayersAsRole(PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList())));
        status = GameStatus.GAME_STARTED;
        this.registerWatchers();
        plugin.getGame().getProgressManager().unlockProgress(GameProgress.GAME_STARTING);
    }

    public void switchWorldRuleForReady(boolean ready) {
        if (!ready) {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setGameRule(GameRule.DO_FIRE_TICK, false);
                world.setGameRule(GameRule.MOB_GRIEFING, false);
                difficultyMap.put(world, world.getDifficulty());
                world.setDifficulty(Difficulty.PEACEFUL);
            });
        } else {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
                world.setGameRule(GameRule.DO_FIRE_TICK, true);
                world.setGameRule(GameRule.MOB_GRIEFING, true);
                world.setDifficulty(difficultyMap.getOrDefault(world, Difficulty.NORMAL));
            });
        }
    }

    public void stop(PlayerRole winner, Location location) {
        this.inGamePlayers.stream().filter(Player::isOnline).forEach(player -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(location.clone().add(0, 3, 0));
            player.teleport(Util.lookAt(player.getEyeLocation(), location));
        });
        this.status = GameStatus.ENDED;
        Bukkit.broadcastMessage(ChatColor.YELLOW + "游戏结束! 服务器将在30秒后重新启动！");
        String runnerNames = Util.list2String(getPlayersAsRole(PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList()));
        String hunterNames = Util.list2String(getPlayersAsRole(PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList()));

        if (winner == PlayerRole.HUNTER) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "胜利者：猎人");
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "恭喜：" + hunterNames);
            getPlayersAsRole(PlayerRole.HUNTER).forEach(player -> player.sendTitle(ChatColor.GOLD + "胜利", "成功击败了逃亡者", 0, 2000, 0));
            getPlayersAsRole(PlayerRole.RUNNER).forEach(player -> player.sendTitle(ChatColor.RED + "游戏结束", "不幸阵亡", 0, 2000, 0));
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + "胜利者：逃亡者");
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "恭喜：" + runnerNames);
            getPlayersAsRole(PlayerRole.RUNNER).forEach(player -> player.sendTitle(ChatColor.GOLD + "胜利", "成功战胜了末影龙", 0, 2000, 0));
            getPlayersAsRole(PlayerRole.HUNTER).forEach(player -> player.sendTitle(ChatColor.RED + "游戏结束", "未能阻止末影龙死亡", 0, 2000, 0));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            GameRecord.stop(this);
            Bukkit.broadcastMessage("游戏录制已保存：" + GameRecord.getRoundUniqueID());
        }
        new MusicPlayer().playEnding();
        Bukkit.getOnlinePlayers().stream().filter(p -> !inGamePlayers.contains(p)).forEach(p -> p.sendTitle(ChatColor.RED + "游戏结束", "The End", 0, 2000, 0));
        new BukkitRunnable() {
            @Override
            public void run() {
                //开始结算阶段
                StatisticsBaker baker = new StatisticsBaker();
                //计算输出最多的玩家
                getGameEndingData().setDamageOutput(baker.getDamageMaster());
                getGameEndingData().setDamageReceive(baker.getDamageTakenMaster());
                getGameEndingData().setWalkMaster(baker.getWalkingMaster());
                getGameEndingData().setJumpMaster(baker.getJumpMaster());
                getGameEndingData().setTeamKiller(baker.getTeamBadGuy());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sendEndingAnimation();
                    }
                }.runTaskLaterAsynchronously(plugin, 20 * 10);
            }
        }.runTaskLater(MineHunt.getInstance(), 20 * 10);
    }

    @SneakyThrows
    private void sendEndingAnimation() {
        double maxCanCost = 20000d;
        int needShows = 0;
        if (StringUtils.isNotBlank(gameEndingData.getDamageOutput())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getDragonKiller())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getDamageReceive())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getStoneAgePassed())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getRunnerKiller())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getWalkMaster())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getJumpMaster())) {
            needShows++;
        }
        maxCanCost /= needShows;

        int sleep = (int) maxCanCost;

        if (StringUtils.isNotBlank(gameEndingData.getDragonKiller())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GOLD + plugin.getConfig().getString("DragonKiller"), gameEndingData.getDragonKiller(), 0, 20000, 0));
            Thread.sleep(sleep);
        }

        if (StringUtils.isNotBlank(gameEndingData.getRunnerKiller())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.RED + plugin.getConfig().getString("RunnerKiller"), gameEndingData.getRunnerKiller(), 0, 20000, 0));
            Thread.sleep(sleep);
        }

        if (StringUtils.isNotBlank(gameEndingData.getDamageOutput())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.AQUA + plugin.getConfig().getString("DamageOutPut"), gameEndingData.getDamageOutput(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getDamageReceive())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.LIGHT_PURPLE + plugin.getConfig().getString("DamageReceive"), gameEndingData.getDamageReceive(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getTeamKiller())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.DARK_RED + plugin.getConfig().getString("TeamKiller"), gameEndingData.getTeamKiller(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getWalkMaster())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.YELLOW + plugin.getConfig().getString("WalkMaster"), gameEndingData.getWalkMaster(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getJumpMaster())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GRAY + plugin.getConfig().getString("JumpMaster"), gameEndingData.getJumpMaster(), 0, 20000, 0));
            Thread.sleep(sleep);
        }

        Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GREEN + plugin.getConfig().getString("EndText1"), plugin.getConfig().getString("EndText2"), 0, 20000, 0));
        Thread.sleep(sleep);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GREEN + plugin.getConfig().getString("ServerName"), plugin.getConfig().getString("ServerGame"), 0, 20000, 0));
        Thread.sleep(sleep);
        Bukkit.getOnlinePlayers().forEach(Player::resetTitle);
        Bukkit.shutdown();
    }

    private void registerWatchers() {
        new RadarWatcher();
        new ReconnectWatcher();
        new PlayerMoveWatcher();
    }

    public List<Player> getPlayersAsRole(PlayerRole role) {
        return this.roleMapping.entrySet().stream().filter(playerPlayerRoleEntry -> playerPlayerRoleEntry.getValue() == role).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    //Code from ManHunt

    private Location airDrop(Location spawnpoint) {
        Location loc = spawnpoint.clone();
        loc = new Location(loc.getWorld(), loc.getBlockX(), 0, loc.getBlockZ());
        Random random = new Random();
        loc.add(random.nextInt(XRandom) + XBasic, 0, random.nextInt(YRandom) + YBasic);
        loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
        loc.getBlock().setType(Material.GLASS);
        loc.setY(loc.getY() + 1);
        return loc;
    }
}
