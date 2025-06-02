package cn.spirit.go.service;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.entity.BaseEntity;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class BaseService<T extends BaseEntity> {

    public Class<T> clazz;

    protected final Collector<Row, ?, List<T>> COLLECTOR = Collectors.mapping(this::mapping, Collectors.toList());

    public T mapping(Row row) {

        T obj;
        try {
            obj = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            String fieldName = StringUtils.camelToSnake(field.getName(), '_');
            int idx = row.getColumnIndex(fieldName);
            if (idx != -1) {
                Object v;
                if (field.getType().isEnum()) {
                    v = Enum.valueOf(field.getType().asSubclass(Enum.class), row.getValue(idx).toString());
                } else {
                    v = row.getValue(idx);
                }
                try {
                    field.set(obj, v);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return obj;
    }

    public BaseService(Class<T> clazz) {
        this.clazz = clazz;
    }

    public String tableName() {
        return clazz.getSimpleName();
    }

    public Future<List<T>> selectList(Tuple tuple) {
        String sql = "SELECT * FROM " + tableName();

        return AppContext.SQL_POOL.getConnection()
                .flatMap(conn -> {
                    if (tuple == null || tuple.size() == 0) {
                        return conn.query(sql).collecting(COLLECTOR).execute().onComplete(ar -> conn.close());
                    }
                    return conn.preparedQuery(sql).collecting(COLLECTOR).execute(tuple).onComplete(ar -> conn.close());
                })
                .map(SqlResult::value);
    }

    public Future<List<T>> selectList() {
        return selectList(null);
    }

    public Future<T> selectById(Integer id) {
        String sql = "SELECT * FROM " + tableName() + " WHERE id = ?";
        return selectOne(sql, Tuple.of(id));
    }

    public Future<T> selectOne(String sql, Tuple tuple) {
        return AppContext.SQL_POOL.getConnection()
                .flatMap(conn -> {
                    if (tuple == null || tuple.size() == 0) {
                        return conn.query(sql).mapping(this::mapping).execute().onComplete(ar -> conn.close());
                    }
                    return conn.preparedQuery(sql).mapping(this::mapping).execute(tuple).onComplete(ar -> conn.close());
                })
                .flatMap(rs -> rs.size() == 1 ?  Future.succeededFuture(rs.iterator().next()) : Future.failedFuture("The SQL query is for " + rs.size() + " records."));
    }

}
