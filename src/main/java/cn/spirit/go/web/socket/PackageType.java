package cn.spirit.go.web.socket;

public enum PackageType {
    // 系统通知
    SYS,
    // 游戏开始通知
    GAME_START,
    // 加入房间通知
    GAME_JOIN,
    // 离开房间通知
    GAME_EXIT,
    // 游戏走棋
    GAME_STEP,
    // 游戏聊天
    GAME_CHAT
}
