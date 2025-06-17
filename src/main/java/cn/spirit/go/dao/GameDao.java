package cn.spirit.go.dao;

import cn.spirit.go.common.enums.ChessPiece;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.entity.GameEntity;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.stream.Collector;

public class GameDao {

    private GameEntity mapping(Row row) {
        GameEntity entity = new GameEntity();
        entity.id = row.getInteger("id");
        entity.code = row.getString("code");
        entity.boardSize = row.getInteger("board_size");
        entity.type = GameType.valueOf(row.getString("type"));
        entity.mode = GameMode.valueOf(row.getString("mode"));
        entity.duration = row.getInteger("duration");
        entity.stepDuration = row.getInteger("step_duration");
        entity.startTime = row.getLocalDateTime("start_time");
        entity.endTime = row.getLocalDateTime("end_time");
        entity.winner = ChessPiece.valueOf(row.getString("winner"));
        entity.camp = ChessPiece.valueOf(row.getString("camp"));
        entity.opponentId = row.getInteger("opponent_id");
        entity.userId = row.getInteger("user_d");
        entity.createdAt = row.getLocalDateTime("created_at");
        entity.updatedAt = row.getLocalDateTime("updated_at");
        return entity;
    }


    public Future<Long> insert(GameEntity entity) {
        return AppContext.SQL_POOL.preparedQuery("INSERT INTO `t_game` (code, board_size, type, mode, duration, step_duration, start_time, end_time, winner, camp, opponent_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                 .collecting(Collector.of(() -> null, (v, row) -> {}, (a, b) -> null))
                .execute(Tuple.of(entity.code, entity.boardSize, entity.type, entity.mode, entity.duration, entity.stepDuration, entity.startTime, entity.endTime, entity.winner, entity.camp, entity.opponentId, entity.userId))
                 .map(row -> row.property(MySQLClient.LAST_INSERTED_ID));
    }

}
