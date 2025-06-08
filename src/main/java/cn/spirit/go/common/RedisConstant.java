package cn.spirit.go.common;

public interface RedisConstant {

    /**
     * Session 过期时间 3 天
     */
    String AUTH_SESSION_EXPIRE = "259200";

    /**
     * Session
     */
    String AUTH_SESSION = "AUTH:SESSION:";


    /**
     * 验证码过期时间 5 分钟
     */
    String CODE_EXPIRE = "300000";

    /**
     * 注册验证码
     */
    String AUTH_CODE_SIGNUP = "AUTH:CODE:SIGNUP:";


    /**
     * 游戏对局
     */
    String GAME_INFO = "GAME_INFO:";
}
