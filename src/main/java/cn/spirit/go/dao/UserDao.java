package cn.spirit.go.dao;

import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import java.util.stream.Collector;

public class UserDao {

    public UserEntity mapping(Row row) {
        UserEntity entity = new UserEntity();
        entity.avatar = row.getString("avatar");
        entity.username = row.getString("username");
        entity.email = row.getString("email");
        entity.password = row.getString("password");
        entity.status = UserStatus.valueOf(row.getString("status"));
        entity.nickname = row.getString("nickname");
        entity.createdAt = row.getLocalDateTime("created_at");
        return entity;
    }

    public Future<String> insert(UserEntity entity) {
        return AppContext.SQL_POOL.preparedQuery("INSERT INTO t_user (avatar, nickname, email, status, username, password) VALUES (?, ?, ?, ?, ?, ?)")
                .collecting(Collector.of(() -> null, (v, row) -> {}, (a, b) -> null))
                .execute(Tuple.of(entity.avatar, entity.nickname, entity.email, entity.status, entity.username, entity.password))
                .map(row -> entity.username);
    }

    public Future<String> updatePassword(String username, String password) {
        return AppContext.SQL_POOL.preparedQuery("UPDATE t_user SET password = ? WHERE username = ?")
                .execute(Tuple.of(username, password))
                .map(row -> username);
    }

    public Future<UserEntity> selectByUsername(String username) {
        return selectOne("SELECT * FROM t_user WHERE username = ?", Tuple.of(username));
    }

    public Future<UserEntity> selectByEmail(String email) {
        return selectOne("SELECT * FROM t_user WHERE email = ?", Tuple.of(email));
    }

    public Future<UserEntity> selectByUsernameOrEmail(String username, String email) {
        return selectOne("SELECT * FROM t_user WHERE username = ? or email = ?", Tuple.of(username, email));
    }



//    public Future<String> insert(UserEntity entity) {
//        JsonObject obj = new JsonObject();
//        if (entity.username != null) {
//            obj.put("username", entity.username);
//        }
//        if (entity.password != null) {
//            obj.put("password", entity.password);
//        }
//        if (entity.nickname != null) {
//            obj.put("nickname", entity.nickname);
//        }
//        if (entity.avatar != null) {
//            obj.put("avatar", entity.avatar);
//        }
//        if (entity.email != null) {
//            obj.put("email", entity.email);
//        }
//        if (entity.status != null) {
//            obj.put("status", entity.status);
//        }
//        return AppContext.MONGO.save("user", obj).compose(id -> {
//            return Future.succeededFuture(entity.username);
//        });
//    }


//
//
//    public Future<UserEntity> selectByUsername(String username) {
//        return AppContext.MONGO.findOne("user", JsonObject.of("username", username), JsonObject.of()).compose(res -> {
//            if (res == null) {
//                return Future.succeededFuture(null);
//            } else {
//                return Future.succeededFuture(res.mapTo(UserEntity.class));
//            }
//        });
//    }
//
//    public Future<UserEntity> selectByEmail(String email) {
//        return AppContext.MONGO.findOne("user", JsonObject.of("email", email), JsonObject.of()).compose(res -> {
//            if (res == null) {
//                return Future.succeededFuture(null);
//            } else {
//                return Future.succeededFuture(res.mapTo(UserEntity.class));
//            }
//        });
//    }
//
//    public Future<UserEntity> selectByUsernameOrEmail(String username, String email) {
//        JsonObject query = new JsonObject()
//                .put("$or", new JsonArray()
//                        .add(new JsonObject().put("username", username))
//                        .add(new JsonObject().put("email", email)));
//        return AppContext.MONGO.findOne("user", query, JsonObject.of()).compose(res -> {
//            if (res == null) {
//                return Future.succeededFuture(null);
//            } else {
//                return Future.succeededFuture(res.mapTo(UserEntity.class));
//            }
//        });
//    }

    private Future<UserEntity> selectOne(String sql, Tuple tuple) {
        return AppContext.SQL_POOL.preparedQuery(sql).mapping(this::mapping).execute(tuple).flatMap(rs -> {
            if (rs.size() == 1) {
                return Future.succeededFuture(rs.iterator().next());
            } else if (rs.size() == 0) {
                return Future.succeededFuture(null);
            } else {
                return Future.failedFuture("The SQL query is for " + rs.size() + " records.");
            }
        });
    }
}
