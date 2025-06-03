package cn.spirit.go.service;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.entity.BaseEntity;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class BaseService<T extends BaseEntity> {

    private static final Logger log = LoggerFactory.getLogger(BaseService.class);

    private final Class<T> clazz;

    private final String tableName;

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

    public Map<String, Object> mapping(T entity) {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = clazz.getFields();
        try {
            for (Field field : fields) {
                String fieldName = StringUtils.camelToSnake(field.getName(), '_');
                Object o = field.get(entity);
                if (o != null) {
                    map.put(fieldName, o);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public String formatParams(Collection<?> list) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                sb.append("?)");
            } else {
                sb.append("?,");
            }
        }
        return sb.toString();
    }

    public String placeholderParams(Integer size) {
        return "(" + "?,".repeat(Math.max(0, size)) + ")";
    }

    public BaseService(Class<T> clazz) {
        this.clazz = clazz;
        this.tableName = AppContext.TABLE_PREFIX + StringUtils.camelToSnake(clazz.getSimpleName(), '_');
    }

    public Future<List<T>> selectList(Tuple tuple) {
        String sql = "SELECT * FROM " + tableName;
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
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
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
                .flatMap(rs -> {
                    if (rs.size() == 1) {
                        return Future.succeededFuture(rs.iterator().next());
                    } else if (rs.size() == 0) {
                        return Future.succeededFuture(null);
                    } else {
                        return Future.failedFuture("The SQL query is for " + rs.size() + " records.");
                    }
                });
    }

    public Future<Long> insert(T entity) {
        Map<String, Object> params = mapping(entity);
        String sql = "INSERT INTO " + tableName + formatParams(params.keySet()) +" VALUES " + placeholderParams(params.size());
        return insert(sql, Tuple.of(params.values()));
    }

    public Future<Long> insert(String sql, Tuple tuple) {
        log.info("SQL Template: {} , params: {}", sql, tuple.deepToString());
        return AppContext.SQL_POOL.getConnection()
                .flatMap(conn -> conn.preparedQuery(sql)
                        .collecting(Collector.of(() -> null, (v, row) -> {}, (a, b) -> null))
                        .execute(tuple)
                        .onComplete(ar -> conn.close())
                )
                .map(row -> row.property(MySQLClient.LAST_INSERTED_ID));
    }
}
