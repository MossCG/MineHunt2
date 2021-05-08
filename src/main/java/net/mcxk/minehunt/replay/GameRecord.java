package net.mcxk.minehunt.replay;

import lombok.Getter;
import me.jumper251.replay.api.ReplayAPI;
import net.mcxk.minehunt.game.Game;
import org.bukkit.Bukkit;

import java.util.UUID;

public class GameRecord {
    @Getter
    private final static UUID roundUniqueID = UUID.randomUUID();

    public static void record(Game game) {
        if (!Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            return;
        }
        ReplayAPI.getInstance().registerReplaySaver(new MHRecordSaver(game));
        ReplayAPI.getInstance().recordReplay(roundUniqueID.toString().replace("-", ""), Bukkit.getConsoleSender());
    }

    public static void stop(Game game) {
        if (!Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            return;
        }
        ReplayAPI.getInstance().stopReplay(roundUniqueID.toString().replace("-", ""), true);
    }
}
