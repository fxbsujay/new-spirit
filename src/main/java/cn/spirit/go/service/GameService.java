package cn.spirit.go.service;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.ChessPiece;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameStatus;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.util.DateUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.dto.GameDTO;
import cn.spirit.go.model.entity.GameEntity;
import cn.spirit.go.model.entity.GameReadyEntity;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameService extends BaseService<GameEntity> {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private Integer dailyGameCount = 0;

    private String dailyTime = DateUtils.getTime("yyyyMMdd");

    public GameService() {
        super(GameEntity.class, "t_game");
    }

    @Override
    public GameEntity mapping(Row row) {
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
        String sql = "INSERT INTO t_game (code, board_size, type, mode, duration, step_duration, start_time, end_time, winner, camp, opponent_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return insert(sql, Tuple.of(entity.code, entity.boardSize, entity.type, entity.mode, entity.duration, entity.stepDuration, entity.startTime, entity.endTime, entity.winner, entity.camp, entity.opponentId, entity.userId));
    }

    public Future<Long> insertReady(GameReadyEntity entity) {
        String sql = "INSERT INTO t_game_ready (name, code, board_size, type, mode, duration, step_duration, start_time, end_time, winner, camp, opponent_id, user_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return insert(sql, Tuple.of(entity.name, entity.code, entity.boardSize, entity.type, entity.mode, entity.duration, entity.stepDuration, entity.startTime, entity.endTime, entity.winner, entity.camp, entity.opponentId, entity.userId, entity.status));
    }

    /**
     * 创建对局
     */
    public void createGame(RestContext<GameDTO, Boolean> ctx) {
        GameDTO dto = ctx.param();


        AppContext.SQL_POOL.preparedQuery("SELECT COUNT(*) FROM t_game_ready WHERE user_id = ?")
                .execute(Tuple.of(1)).map(rows -> {
                    return 0;
                }).onSuccess(integer -> {

                }).onFailure(e -> {

                });

        GameReadyEntity entity = new GameReadyEntity();
        entity.name = dto.name;
        entity.code = generateCode();
        entity.status = GameStatus.READY;
        entity.type = dto.type;
        entity.mode = dto.mode;
        entity.boardSize = dto.boardSize;
        entity.duration = dto.duration;
        entity.stepDuration = dto.stepDuration;
        entity.userId = dto.userId;

        insertReady(entity).onSuccess(id -> {
            ctx.success(true);
        }).onFailure(e -> {
            log.error("create game failed {}", e.getMessage());
            ctx.fail();
        });
    }

    /**
     * 生成对局唯一编码
     * 当前日期 + 机器码 + 当日创建次数
     * 20250608 + 001 + 2
     */
    public String generateCode() {
        String time = DateUtils.getTime("yyyyMMdd");
        if (!time.equals(dailyTime)) {
            dailyTime = time;
            dailyGameCount = 0;
        }
        dailyGameCount++;
        return Long.toString(Long.parseLong(dailyTime.substring(2) + AppContext.MAC_CODE + dailyGameCount), 36).toUpperCase();
    }
}
