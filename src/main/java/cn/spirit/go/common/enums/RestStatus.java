package cn.spirit.go.common.enums;

public interface RestStatus {

    // ===================== 账户相关 =====================

    // 邮箱已被注册
    int EMAIL_IS_EXIST = 10001;

    // 用户名已被注册
    int USERNAME_IS_EXIST = 10002;

    // 账户不存在
    int ACCOUNT_NOT_EXIST = 10003;

    // 密码错误
    int PASSWORD_FAIL = 10004;

    // 账户已被封禁
    int ACCOUNT_BAN = 10005;

    // 验证码过期或已失效
    int CODE_INVALID = 10006;

    // 验证码错误
    int CODE_ERROR = 10007;

    // 验证码已发送
    int CODE_RESEND = 10008;

    // ===================== 游戏相关 =====================

    // 已创建对局
    int GAME_CREATED = 20001;

    // 对局不存在
    int GAME_NOT_EXIST = 20002;

    // 对局已开始
    int GAME_STARTED = 20003;

}
