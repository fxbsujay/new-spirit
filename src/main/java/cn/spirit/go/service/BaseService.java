package cn.spirit.go.service;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.entity.BaseEntity;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLClient;
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

    public Object[] mapping(T entity) {
        Field[] fields = clazz.getFields();

        StringBuilder column = new StringBuilder("(");
        StringBuilder placeholder = new StringBuilder("(");
        Object[] values = new Object[fields.length];
        int length = 0;

        try {
            for (Field field : fields) {
                Object o = field.get(entity);
                if (o != null) {
                    if (length != 0) {
                        column.append(",");
                        placeholder.append(",");
                    }
                    column.append(StringUtils.camelToSnake(field.getName(), '_'));
                    placeholder.append("?");
                    values[length] = o;
                    length++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        column.append(")");
        placeholder.append(")");

        if (length != values.length) {
           values = Arrays.copyOf(values, length);
        }

        return new Object[]{column, placeholder, Tuple.from(values)};
    }

    public BaseService(Class<T> clazz, String tableName) {
        this.clazz = clazz;
        this.tableName = tableName;
    }

    public Future<List<T>> selectList(String sql, Tuple tuple) {
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
        return selectList( "SELECT * FROM " + tableName, null);
    }

    public Future<T> selectById(Integer id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return selectOne(sql, Tuple.of(id));
    }

    public Future<T> selectOne(String sql, Tuple tuple) {
        return AppContext.SQL_POOL.getConnection()
                .flatMap(conn -> {
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
        Object[] mapping = mapping(entity);
        String sql = "INSERT INTO " + tableName + mapping[0] +" VALUES " + mapping[1];
        return insert(sql, (Tuple) mapping[2]);
    }

    public Future<Long> insert(String sql, Tuple tuple) {
        return AppContext.SQL_POOL.getConnection()
                .flatMap(conn -> conn.preparedQuery(sql)
                        .collecting(Collector.of(() -> null, (v, row) -> {}, (a, b) -> null))
                        .execute(tuple)
                        .onComplete(ar -> conn.close())
                )
                .map(row -> row.property(MySQLClient.LAST_INSERTED_ID));
    }
}
