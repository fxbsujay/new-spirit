package cn.spirit.go.common;

public interface RedisConstant {

    /**
     * Session
     */
    String AUTH_SESSION = "AUTH:SESSION:";

    /**
     * 注册验证码
     */
    String AUTH_CODE_SIGNUP = "AUTH:CODE:SIGNUP:";

    /**
     * 验证码过期时间 5 分钟
     */
    String CODE_EXPIRE = "300000";

    /**
     * 游戏对局
     */
    String GAME_INFO = "GAME_INFO:";
}
