package cn.spirit.go.model;

import cn.spirit.go.common.enums.GameWinner;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;

public class GamePlay {

    /**
     * 编号
     */
    public String code;

    /**
     * 棋盘大小
     */
    public Integer boardSize;

    /**
     * 类型 SHORT 实时的短时长 LONG 通讯长时长 NONE 无限制
     */
    public GameType type;

    /**
     * 模式 CASUAL 休闲 RANK 积分 ROBOT 人机 FRIEND 好友
     */
    public GameMode mode;

    /**
     * 起始时长 单位：毫秒
     */
    public Integer duration;

    /**
     * 步长；如果Type为SHORT则为每步加时，如果为LONG则为每步限时，如果为NONE则为0 单位：毫秒
     */
    public Integer stepDuration;

    /**
     * 创建时间
     */
    public Long timestamp;

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
    public GameWinner winner;

    /**
     * 白棋用户
     */
    public String white;

    /**
     * 黑棋用户
     */
    public String black;
}
