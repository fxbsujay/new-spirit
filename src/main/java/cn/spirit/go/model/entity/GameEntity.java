package cn.spirit.go.model.entity;

import cn.spirit.go.common.enums.ChessPiece;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import java.time.LocalDateTime;

public class GameEntity extends BaseEntity {

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
   * 起始时长 单位：秒
   */
  public Integer duration;

  /**
   * 步长；如果Type为SHORT则为每步加时，如果为LONG则为每步限时，如果为NONE则为0 单位：秒
   */
  public Integer stepDuration;

  /**
   * 开始时间
   */
  public LocalDateTime startTime;

  /**
   * 开始时间
   */
  public LocalDateTime endTime;

  /**
   * 获胜方 WHITE BLACK
   */
  public ChessPiece winner;

  /**
   * 我的阵营 WHITE 白棋 BLACK 黑棋
   */
  public ChessPiece camp;

  /**
   * 对手ID
   */
  public Integer opponentId;

  /**
   * 所属人
   */
  public Integer userId;
}
