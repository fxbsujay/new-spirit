package cn.spirit.go.common.enums;

import cn.spirit.go.common.util.StringUtils;

public enum GameMode {

    /**
     * 休闲
     */
    CASUAL,
    /**
     * 积分
     */
    RANK,
    /**
     * 人机
     */
    ROBOT,
    /**
     * 好友
     */
    FRIEND;

    public static GameMode convert(final String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (GameMode value : values()) {
            if (value.toString().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
