package cn.spirit.go.model;

import cn.spirit.go.common.enums.ChessPiece;

public class GamePlay extends GameWait {
    /**
     * 开始时间
     */
    public Long startTime;

    /**
     * 结束时间
     */
    public Long endTime;

    /**
     * 获胜方 WHITE BLACK
     */
    public ChessPiece winner;

    /**
     * 白棋用户
     */
    public String white;

    /**
     * 黑棋用户
     */
    public String black;
}
