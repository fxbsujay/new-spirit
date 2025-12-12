package cn.spirit.go.model;

import java.util.Objects;

public class GameStep {

    public Integer x;

    public Integer y;

    public Long timestamp;

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

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}
