package cn.spirit.go.common;

public interface LockConstant {

    /**
     * 游戏对局操作锁，创建游戏，删除游戏对局
     */
    String GAME_LOCK = "GAME:LOCK:";

    /**
     * 房间对局操作锁，落子，取消对局，认输，等
     */
    String ROOM_LOCK = "GAME:LOCK:";
}
