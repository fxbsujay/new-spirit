package cn.spirit.go.model.dto;

import java.util.Objects;

public class GameStepDTO {

    public Integer x;

    public Integer y;

    public Long timestamp;

    public GameStepDTO(Integer x, Integer y) {
        this.x = x;
        this.y = y;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GameStepDTO that = (GameStepDTO) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
