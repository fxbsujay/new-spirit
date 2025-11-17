package cn.spirit.go.service;

import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.util.DateUtils;
import cn.spirit.go.model.dto.GameWaitDTO;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.ClientManger;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 没有  {@link ClientManger Socket} 连接不能创建游戏<br/>
 * 创建的游戏对局，等待对手加入游戏，游戏开始后删除游戏，用户连接断开后删除<br/>
 * 游戏未开始只存在内存中，不保存数据，游戏开始后保存数据
 */
public class GameWaitService {

    private static final Logger log = LoggerFactory.getLogger(GameWaitService.class);

    private Integer dailyGameCount = 0;

    private String dailyTime = DateUtils.getTime("yyyyMMdd");

    /**
     * username -> code
     */
    private final Map<String, String> userGames = new HashMap<>();

    /**
     * code -> game
     */
    private final Map<String, GameWaitDTO> games = new HashMap<>();

    /**
     * 分布式锁
     */
    private final String GAME_LOCK = "GAME:LOCK:";

    /**
     * 搜索休闲游戏
     *
     * @param username  查询不是自己的对局
     * @param like      对局名称或编号
     * @param type      {@link GameType}
     */
    public List<GameWaitDTO> searchGames(String username, String like, GameType type, int limit) {
        List<GameWaitDTO> result = new ArrayList<>();
        for (GameWaitDTO value : games.values()) {
            if (null != username && (username.equals(value.username)) ||
                (null != like&& !like.equals(value.code) && !like.equals(value.username)) ||
                (null != type && type != value.type) ||
                !GameMode.CASUAL.equals(value.mode)) {
                continue;
            }
            result.add(value);
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    /**
     * 创建游戏
     * @param session   Session
     * @param game      对局
     */
    public Future<Boolean> addGame(UserSession session, GameWaitDTO game) {
        return AppContext.vertx.sharedData().withLock(GAME_LOCK + session.username, 1000, () -> {
            if (userGames.containsKey(session.username)) {
                log.warn("{} failed to create the game", session.username);
                return Future.succeededFuture(false);
            }
            game.timestamp = System.currentTimeMillis() / 1000;
            game.username = session.username;
            String code = generateCode();
            game.code = code;
            userGames.put(game.username, code);
            games.put(code, game);
            log.info("{} has created a game, code = {}", game.username, code);
            return Future.succeededFuture(true);
        });
    }

    /**
     * 删除自己的游戏
     * socket断开时删除、自己取消游戏时删除、别人加入游戏时删除
     *
     * @param username  用户名
     */
    public Future<GameWaitDTO> removeGame(String username) {
        return AppContext.vertx.sharedData().withLock(GAME_LOCK + username, 1000, () -> {
            String code = userGames.remove(username);
            if (null != code) {
                return Future.succeededFuture(games.remove(code));
            }
            return Future.succeededFuture(null);
        });
    }

    public GameWaitDTO get(String code) {
        return games.get(code);
    }

    public GameWaitDTO getByUsername(String username) {
        String code = userGames.get(username);
        if (null == code) {
            return null;
        }
        return get(code);
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
