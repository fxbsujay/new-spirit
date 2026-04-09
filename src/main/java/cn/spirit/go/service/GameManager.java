package cn.spirit.go.service;

import cn.spirit.go.common.LockConstant;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.util.DateUtils;
import cn.spirit.go.model.CasualGameInfo;
import cn.spirit.go.model.Page;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.ClientManger;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 游戏对局管理器
 */
public class GameManager {

    private static final Logger log = LoggerFactory.getLogger(GameManager.class);

    private Integer dailyGameCount = 0;

    private String dailyTime = DateUtils.getTime("yyyyMMdd");

    /**
     * 大厅
     */
    private final GameLobbyService lobbyService = new GameLobbyService();

    /**
     * 排位
     */
    private final GameRankedService rankedService = new GameRankedService();

    /**
     * session 客户端
     */
    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    /**
     * 对局房间
     */
    private final GoMatchService matchService = new GoMatchService();

    /**
     * 搜索游戏大厅
     */
    public Page<CasualGameInfo> searchLobbyGames(UserSession session, GameType type, int page) {
        Page<CasualGameInfo> result = new Page<>();
        List<CasualGameInfo> games = new ArrayList<>();
        if (page < 0) {
            page = 0;
        }
        if (session.visitor) {
            for (CasualGameInfo game : lobbyService.getGames()) {
                if (null != type && type != game.type) {
                    continue;
                }
                games.add(game);
            }
        } else {
            for (CasualGameInfo game : lobbyService.getGames()) {
                if (session.username.equals(game.username) || (null != type && type != game.type)) {
                    continue;
                }
                games.add(game);
            }
        }
        int min = page * 10;
        if (games.isEmpty() || min > games.size()) {
            result.page = page;
            return result;
        }
        int max;
        if (!session.visitor) {
            CasualGameInfo userGame = lobbyService.getByUsername(session.username);
            if (null != userGame) {
                max = min + 9;
                result.list.add(userGame);
            } else {
                max = min + 10;
            }
        } else {
           max = min + 10;
        }
        result.total = games.size();
        result.list.addAll(games.subList(min, Math.min(max, games.size())));
        result.page = page;
        return result;
    }

    public CasualGameInfo getCasualGame(String code) {
        return lobbyService.get(code);
    }

    public CasualGameInfo getCasualGameByUsername(String username) {
        return lobbyService.getByUsername(username);
    }

    /**
     * 创建休闲比赛
     *
     * @param game 比赛信息
     * @return 是否创建成功
     */
    public Future<Boolean> createCasualGame(CasualGameInfo game) {
        game.code = generateCode();
        return lock(game.username, () -> {
            // 客户端没有链接或者在排位匹配中无法创建休闲对局
            if (!clientManger.isOnLine(game.username) || rankedService.isMatching(game.username)) {
                return Future.succeededFuture(false);
            }
            return Future.succeededFuture(lobbyService.addGame(game));
        });
    }

    /**
     * 取消自己创建的休闲比赛
     *
     * @param username  用户名
     * @return 取消的比赛信息
     */
    public Future<CasualGameInfo> cancelCasualGame(String username) {
        return lock(username, () -> Future.succeededFuture(lobbyService.removeGame(username)));
    }

    /**
     * 取消自己所有等待中的比赛,大厅创建的比赛以及排放比赛
     *
     * @param username  用户名
     */
    public void cancelAllWaitGame(String username) {
       lock(username, () -> {
            lobbyService.removeGame(username);
            rankedService.cancel(username);
            return Future.succeededFuture();
        });
    }

    /**
     * 开始排位
     */
    public Future<Boolean> startRanking(String username, int rating) {
        return lock(username, () -> {
            if (!clientManger.isOnLine(username) || lobbyService.getByUsername(username) != null) {
                return Future.succeededFuture(false);
            }
            return Future.succeededFuture(rankedService.ranking(username, rating));
        });
    }

    /**
     * 取消排位
     */
    public Future<Boolean> cancelRanking(String username) {
        return lock(username, () -> Future.succeededFuture(rankedService.cancel(username)));
    }

    /**
     * 玩家是否在寻找对局中
     *
     * @param username  用户名
     */
    public Boolean isExistWaitGame(String username) {
        return lobbyService.getByUsername(username) != null || rankedService.isMatching(username);
    }

    private synchronized <T> Future<T> lock(String username, Supplier<Future<T>> block) {
        return AppContext.withLock(LockConstant.GAME_LOCK + username, block);
    }

    /**
     * 生成对局唯一编码
     * 当前日期 + 机器码 + 当日创建次数
     * 20250608 + 001 + 2
     */
    private String generateCode() {
        String time = DateUtils.getTime("yyyyMMdd");
        if (!time.equals(dailyTime)) {
            dailyTime = time;
            dailyGameCount = 0;
        }
        dailyGameCount++;
        return Long.toString(Long.parseLong(dailyTime.substring(2) + AppContext.MAC_CODE + dailyGameCount), 36).toUpperCase();
    }
}
