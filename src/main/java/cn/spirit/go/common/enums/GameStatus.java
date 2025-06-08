package cn.spirit.go.common.enums;

public enum GameStatus {
    /**
     * 刚创建的，可搜索
     */
    READY,
    /**
     * 已经开始，等等落子
     */
    START,
    /**
     * 游戏中
     */
    PLAYING,
    /**
     * 结束
     */
    END
}
