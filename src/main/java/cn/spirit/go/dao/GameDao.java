package cn.spirit.go.dao;

import cn.spirit.go.common.enums.ChessPiece;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.model.entity.GameEntity;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class GameDao {

    private JsonObject mapping(GameEntity entity) {
        JsonObject obj = JsonObject.of();
        if (entity.code != null) {
            obj.put("code", entity.code);
        }
        if (entity.boardSize != null) {
            obj.put("boardSize", entity.boardSize);
        }
        if (entity.type != null) {
            obj.put("type", entity.type);
        }
        if (entity.mode != null) {
            obj.put("mode", entity.mode);
        }
        if (entity.duration != null) {
            obj.put("duration", entity.duration);
        }
        if (entity.stepDuration != null) {
            obj.put("stepDuration", entity.stepDuration);
        }
        if (entity.startTime != null) {
            obj.put("startTime", entity.startTime);
        }
        if (entity.endTime != null) {
            obj.put("endTime", entity.endTime);
        }
        if (entity.winner != null) {
            obj.put("winner", entity.winner);
        }
        if (entity.white != null) {
            obj.put("white", entity.white);
        }
        if (entity.black != null) {
            obj.put("black", entity.black);
        }
        return obj;
    }

    private GameEntity mapping(JsonObject obj) {
        if (null == obj) {
            return null;
        }
        GameEntity entity = new GameEntity();
        entity.code = obj.getString("code");
        entity.boardSize = obj.getInteger("boardSize");
        entity.type = GameType.valueOf(obj.getString("type"));
        entity.mode = GameMode.valueOf(obj.getString("type"));
        entity.duration = obj.getInteger("duration");
        entity.stepDuration = obj.getInteger("stepDuration");
        entity.startTime = obj.getLong("startTime");
        entity.endTime = obj.getLong("endTime");
        entity.winner = ChessPiece.valueOf(obj.getString("code"));
        entity.white = obj.getString("white");
        entity.black = obj.getString("black");
        return entity;
    }

    public Future<GameEntity> selectByCode(String code) {
        return AppContext.MONGO.findOne("game", JsonObject.of("code", code), JsonObject.of()).compose(res -> Future.succeededFuture(mapping(res)));
    }


    public Future<String> insert(GameEntity entity) {
        return AppContext.MONGO.save("game", mapping(entity));
    }

}
