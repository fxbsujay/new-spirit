package cn.spirit.go.model.entity;

import cn.spirit.go.common.enums.GameStatus;

public class GameReadyEntity extends GameEntity {

  public String name;

  /**
   * 游戏状态 READY 刚创建的 START 开始（等待落子） PLAYING 游戏中 END 结束
   */
  public GameStatus status;

}
