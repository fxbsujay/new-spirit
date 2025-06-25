package cn.spirit.go.dao;

import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameStatus;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.dto.SearchGameDTO;
import cn.spirit.go.model.entity.GameReadyEntity;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class GameReadyDao {

    private GameReadyEntity mapping(Row row) {
        GameReadyEntity entity = new GameReadyEntity();
        entity.name = row.getString("name");
        entity.code = row.getString("code");
        entity.status = GameStatus.valueOf(row.getString("status"));
        entity.boardSize = row.getInteger("board_size");
        entity.type = GameType.valueOf(row.getString("type"));
        entity.mode = GameMode.valueOf(row.getString("mode"));
        entity.duration = row.getInteger("duration");
        entity.stepDuration = row.getInteger("step_duration");
        entity.username = row.getString("username");
        entity.score = row.getInteger("score");
        return entity;
    }

    public Future<List<SearchGameDTO>> searchPage(String name, GameMode mode, GameType type, String username) {
        String sql = "SELECT g.*, u.nickname FROM (SELECT * FROM t_game_ready WHERE status = ?";
        Tuple tuple = Tuple.of(GameStatus.READY);

        if (StringUtils.isNotBlank(username)) {
            sql += " AND username != ?";
            tuple.addString(username);
        }
        if (StringUtils.isNotBlank(name)) {
            sql += " AND `name` LIKE ?";
            tuple.addString("%" + name + "%");
        }
        if (mode != null) {
            sql += " AND mode = ?";
            tuple.addValue(mode);
        }
        if (type != null) {
            sql += " AND type = ?";
            tuple.addValue(type);
        }

        sql = sql + " ORDER BY RAND()  LIMIT 10 ) g LEFT JOIN t_user u ON u.username = g.username";

        return AppContext.SQL_POOL.preparedQuery(sql)
                .collecting(Collectors.mapping(row -> {
                    SearchGameDTO dto = new SearchGameDTO();
                    dto.name = row.getString("name");
                    dto.code = row.getString("code");
                    dto.boardSize = row.getInteger("board_size");
                    dto.type = GameType.convert(row.getString("type"));
                    dto.mode = GameMode.convert(row.getString("mode"));
                    dto.duration = row.getInteger("duration");
                    dto.stepDuration = row.getInteger("step_duration");
                    dto.username = row.getString("username");
                    dto.nickname = row.getString("nickname");
                    dto.score = row.getInteger("score");
                    return dto;
                }, Collectors.toList()))
                .execute(tuple)
                .map(SqlResult::value);
    }


    /**
     * 根据编号查询对局
     *
     * @param code 游戏编号
     * @return 查询到条数为1返回对局，否则返回错误
     */
    public Future<GameReadyEntity> selectOneByCode(String code) {
        return AppContext.SQL_POOL.preparedQuery("SELECT * FROM `t_game_ready` WHERE code = ?")
                .mapping(this::mapping)
                .execute(Tuple.of(code))
                .flatMap(rs -> {
                    if (rs.size() == 1) {
                        return Future.succeededFuture(rs.iterator().next());
                    }
                    return Future.failedFuture("The SQL query is for " + rs.size() + " records.");
                });
    }

    public Future<Long> selectCountByUsername(String username) {
        return AppContext.SQL_POOL.preparedQuery("SELECT COUNT(*) FROM `t_game_ready` WHERE username = ?").execute(Tuple.of(username)).map(rows -> rows.iterator().next().getLong(0));
    }


    public Future<String> insert(GameReadyEntity entity) {
        return AppContext.SQL_POOL.preparedQuery("INSERT INTO `t_game_ready` (name, code, board_size, type, mode, duration, step_duration, username, status, score) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                 .collecting(Collector.of(() -> null, (v, row) -> {}, (a, b) -> null))
                .execute(Tuple.of(entity.name, entity.code, entity.boardSize, entity.type, entity.mode, entity.duration, entity.stepDuration, entity.username, entity.status, entity.score))
                 .map(row -> entity.code);
    }

}
