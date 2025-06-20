package cn.spirit.go.common;

public interface RedisConstant {

    /**
     * 验证码过期时间 5 分钟
     */
    public static String CODE_EXPIRE = "300000";

    /**
     * 注册验证码
     */
    String AUTH_CODE_SIGNUP = "AUTH:CODE:SIGNUP:";


    String GAME_COUNT = "GAME:COUNT";

    /**
     * 游戏对局
     */
    String GAME_INFO = "GAME_INFO:";
}
