package cn.spirit.go.common.enums;

public enum RestStatus {

    // ===================== 账户相关 =====================

    EMAIL_IS_EXIST(10001, "邮箱已被注册"),

    USERNAME_IS_EXIST(10002, "用户名已被注册"),

    ACCOUNT_NOT_EXIST(10003, "账户不存在"),

    EMAIL_CODE_IS_INVALID(10004, "密码错误"),

    ACCOUNT_BAN(10005, "账户已被封禁"),

    CODE_INVALID(10006, "验证码过期或已失效"),

    CODE_ERROR(10007, "验证码错误"),

    // ===================== 游戏相关 =====================

    GAME_CREATED(20001, "已创建对局"),

    GAME_NOT_EXIST(20001, "对局不存在"),

    GAME_STARTED(20002, "对局已开始"),
    ;

    /**
     * code 编码
     */
    private final Integer code;

    /**
     * 消息内容
     */
    private final String message;

    RestStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "{\"code\":\"" + code + "\", \"message\":\"" + message + "\"}";
    }
}
