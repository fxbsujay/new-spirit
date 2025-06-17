package cn.spirit.go.common.enums;

import cn.spirit.go.common.util.StringUtils;

public enum GameType {

    /**
     * 实时的
     */
    SHORT,
    /**
     * 通讯棋
     */
    LONG,
    /**
     * 无限制
     */
    NONE;


    public static GameType convert(final String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (GameType value : values()) {
            if (value.toString().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
