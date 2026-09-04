package cn.spirit.go.web.socket;

public enum PackageType {
    // 游戏开始通知
    GAME_START,
    // 游戏结束通知
    GAME_END,
    // 认输
    GAME_SURRENDER,
    // 求和
    GAME_PEACE,

    // 加入房间通知
    ROOM_CONNECTION,
    // 离开房间通知
    ROOM_DISCONNECTION,
    // 已经连接了不允许再次连接
    ROOM_CONNECTION_EXISTS,
    // 游戏走棋
    ROOM_STEP,
    // 游戏聊天
    ROOM_CHAT,

}
