package cn.spirit.go.model.vo;

import cn.spirit.go.common.enums.UserStatus;

public class UserInfoVO {

    /**
     * 用户头像
     */
    public String avatar;

    /**
     * 用户昵称
     */
    public String nickname;

    /**
     * 用户账号
     */
    public String username;

    /**
     * 状态
     */
    public UserStatus status;

    /**
     * 分数
     */
    public Integer rating;

}
