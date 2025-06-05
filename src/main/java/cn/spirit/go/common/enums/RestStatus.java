package cn.spirit.go.common.enums;

public enum RestStatus {

    EMAIL_IS_EXIST(10001, "邮箱已被注册"),

    USERNAME_IS_EXIST(10002, "用户名已被注册"),

    ACCOUNT_NOT_EXIST(10003, "账户不存在"),

    EMAIL_CODE_IS_INVALID(10004, "密码错误"),

    PASSWORD_WRONG(10005, "账户已被封禁"),

    SIGNUP_CODE_INVALID(10006, "验证码过期或已失效"),

    SIGNUP_CODE_ERROR(10007, "验证码错误"),
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
}
