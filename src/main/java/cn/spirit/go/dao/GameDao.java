package cn.spirit.go.dao;

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

    public Future<String> insert(GameEntity entity) {
        return AppContext.MONGO.save("game", mapping(entity)).compose(id -> Future.succeededFuture(entity.code));
    }

}
