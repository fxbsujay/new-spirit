package cn.spirit.go.service;

import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UserService extends BaseService<UserEntity> {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService() {
        super(UserEntity.class);
    }

    @Override
    public UserEntity mapping(Row row) {
        UserEntity entity = new UserEntity();
        entity.id = row.getInteger("id");
        entity.avatar = row.getString("avatar");
        entity.username = row.getString("username");
        entity.email = row.getString("email");
        entity.password = row.getString("password");
        entity.status = UserStatus.valueOf(row.getString("status"));
        entity.nickname = row.getString("nickname");
        entity.createdAt = row.getLocalDateTime("created_at");
        entity.updatedAt = row.getLocalDateTime("updated_at");
        return entity;
    }

    @Override
    public Map<String, Object> mapping(UserEntity entity) {
        Map<String, Object> map = new HashMap<>();
        if(entity.id != null) {
            map.put("id", entity.id);
        }
        if(entity.avatar != null) {
            map.put("id", entity.avatar);
        }
        if(entity.username != null) {
            map.put("id", entity.username);
        }
        if(entity.email != null) {
            map.put("id", entity.email);
        }
        if(entity.password != null) {
            map.put("id", entity.password);
        }
        if(entity.status != null) {
            map.put("id", entity.status);
        }
        if(entity.nickname != null) {
            map.put("id", entity.nickname);
        }
        if(entity.createdAt != null) {
            map.put("id", entity.createdAt);
        }
        if(entity.updatedAt != null) {
            map.put("id", entity.updatedAt);
        }
        return map;
    }

    public Future<Long> insert(UserEntity entity) {
        String sql = "INSERT INTO t_user (avatar, nickname, email, status, username, password) VALUES (?, ?, ?, ?, ?, ?)";
        return insert(sql, Tuple.of(entity.avatar, entity.nickname, entity.email, entity.status, entity.username, entity.password));
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
}
