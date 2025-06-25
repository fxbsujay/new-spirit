package cn.spirit.go.model.entity;

import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameStatus;
import cn.spirit.go.common.enums.GameType;

public class GameReadyEntity {

  /**
   * 编号
   */
  public String code;

  /**
   * 名称
   */
  public String name;

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
   * 起始时长 单位：秒
   */
  public Integer duration;

  /**
   * 步长；如果Type为SHORT则为每步加时，如果为LONG则为每步限时，如果为NONE则为0 单位：秒
   */
  public Integer stepDuration;

  /**
   * 游戏状态 READY 刚创建的 START 开始（等待落子） PLAYING 游戏中 END 结束
   */
  public GameStatus status;

  /**
   * 分数
   */
  public Integer score;

  /**
   * 所属人
   */
  public String username;

}
