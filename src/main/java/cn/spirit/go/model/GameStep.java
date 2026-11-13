package cn.spirit.go.model;

import java.util.Objects;

public class GameStep {

    public Integer x;

    public Integer y;

    public Long timestamp;

    public GameStep() {}

    public GameStep(Integer x, Integer y) {
        this.x = x;
        this.y = y;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GameStep that = (GameStep) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    /**
     * 解析
     * @param str 示例 K8-1793612024647
     */
    public static GameStep parse(String str) {
        if (str == null) {
            throw new IllegalArgumentException("input is null");
        }
        str = str.trim().toUpperCase();

        int dash = str.indexOf('-');
        if (dash <= 1 || dash == str.length() - 1) {
            throw new IllegalArgumentException("非法格式: " + str);
        }

        GameStep step = new GameStep();

        // 1. 解析 x：字母 -> LOCATION 下标
        char letter = str.charAt(0);
        int index = -1;
        for (int i = 0; i < Room.LOCATION.length; i++) {
            if (Room.LOCATION[i] == letter) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("非法位置字母: " + letter);
        }
        step.x = index;                // K -> 10（若想从1开始，用 index + 1）

        // 2. 解析 y：字母与 '-' 之间的数字
        step.y = Integer.parseInt(str.substring(1, dash));   // 8

        // 3. 解析 timestamp：'-' 之后的数字
        step.timestamp = Long.parseLong(str.substring(dash + 1)); // 1793612024647L

        return step;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}
