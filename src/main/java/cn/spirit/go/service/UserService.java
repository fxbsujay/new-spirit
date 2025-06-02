package cn.spirit.go.service;

import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.sqlclient.Tuple;

public class UserService extends BaseService<UserEntity> {

    public UserService() {
        super(UserEntity.class);
    }


    public Future<UserEntity> selectByUsername(String username) {
        return selectOne("SELECT * FROM t_user WHERE username = ?", Tuple.of(username));
    }

    public Future<UserEntity> selectByEmail(String email) {
        return selectOne("SELECT * FROM t_user WHERE email = ?", Tuple.of(email));
    }
}
