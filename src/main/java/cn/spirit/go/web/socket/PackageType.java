package cn.spirit.go.web.socket;

public enum PackageType {
    // 游戏开始通知
    GAME_START,
    // 游戏结束
    GAME_END,
    // 加入房间通知
    GAME_CONNECTION,
    // 离开房间通知
    GAME_DISCONNECTION,
    // 游戏走棋
    GAME_STEP,
    // 游戏聊天
    GAME_CHAT
}
