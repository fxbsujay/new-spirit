package cn.spirit.go.common;

public interface LockConstant {

    /**
     * 游戏相关操作锁
     */
    String GAME_LOCK = "GAME:LOCK:";

    /**
     * 房间对局操作锁，落子，取消对局，认输等
     */
    String ROOM_LOCK = "ROOM:LOCK:";
}
