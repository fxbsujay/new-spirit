package cn.spirit.go.service;

import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService extends BaseService<UserEntity> {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService() {
        super(UserEntity.class, "t_user");
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
