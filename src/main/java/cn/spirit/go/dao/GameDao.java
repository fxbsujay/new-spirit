package cn.spirit.go.dao;

import cn.spirit.go.common.enums.ChessPiece;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.model.entity.GameEntity;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import java.util.stream.Collector;

public class GameDao {

    private GameEntity mapping(Row row) {
        GameEntity entity = new GameEntity();
        entity.code = row.getString("code");
        entity.boardSize = row.getInteger("board_size");
        entity.type = GameType.valueOf(row.getString("type"));
        entity.mode = GameMode.valueOf(row.getString("mode"));
        entity.duration = row.getInteger("duration");
        entity.stepDuration = row.getInteger("step_duration");
        entity.startTime = row.getLocalDateTime("start_time");
        entity.endTime = row.getLocalDateTime("end_time");
        entity.winner = ChessPiece.valueOf(row.getString("winner"));
        entity.white = row.getString("white");
        entity.black = row.getString("black");
        return entity;
    }

    public Future<String> insert(GameEntity entity) {
        return AppContext.SQL_POOL.preparedQuery("INSERT INTO `t_game` (code, board_size, type, mode, duration, step_duration, start_time, end_time, winner, white, black) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                 .collecting(Collector.of(() -> null, (v, row) -> {}, (a, b) -> null))
                .execute(Tuple.of(entity.code, entity.boardSize, entity.type, entity.mode, entity.duration, entity.stepDuration, entity.startTime, entity.endTime, entity.winner, entity.white, entity.black))
                 .map(row -> entity.code);
    }

}
