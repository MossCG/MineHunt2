package net.mcxk.minehunt.game;

public enum GameProgress {
    NOT_STARTED("未开始"),
    GAME_STARTING("游戏开始"),
    STONE_AGE("石器时代"),
    IRON_MINED("来硬的!"),
    COMPASS_UNLOCKED("我的方向在哪里?"),
    ENTER_NETHER("我们需要更加深入一点"),
    GET_BLAZE_ROD("与火共舞"),
    GET_ENDER_PERAL("紫色魔力"),
    ENTER_END("结束了?"),
    KILLED_DRAGON("结束了");
    private final String display;

    GameProgress(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
