package cn.spirit.go.service;

import cn.spirit.go.common.LockConstant;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 积分赛匹配池
 * 进来的先寻找，未需找到合适的对手则扩大搜索范围或等待下个加入者
 */
public class GameRankedService {

    private static final Logger log = LoggerFactory.getLogger(GameRankedService.class);

    private final List<Player> matchingQueue = new ArrayList<>();

    private final List<Player> waitingQueue = new ArrayList<>();

    private static final Integer MAX_RANGE = 10000;

    /**
     * 积分赛匹配
     * 玩家不能有其他模式的等待队列，比如大厅已经创建的自定义对局但未开始
     * 当玩家不在线时不允许创建对局 {@link cn.spirit.go.web.socket.ClientManger}
     *
     * @param username  用户名
     * @param rating    积分
     */
    public Future<Boolean> ranking(String username, Integer rating) {
        Player player = new Player(username, rating);
        return AppContext.withLock(LockConstant.GAME_LOCK + username, 200, () -> {
            if (isMatching(username)) {
                return Future.succeededFuture(false);
            }
            matchingQueue.add(player);

            // 开始匹配
            match(player, 200).compose(opponent -> {
                // 匹配完毕
                if (null == opponent) {
                    // 未匹配到玩家加入待匹配队列
                    waitingQueue.add(player);
                    return Future.succeededFuture(true);
                } else {
                    // TODO 匹配到对手，加入待接受对局池，双方都接受对局后开始游戏
                    log.info("Game Matchmaking successful, Players: [{},{}]", player.username, opponent.username);

                    // 删除对手的匹配
                    matchingQueue.remove(opponent);
                }
                return Future.succeededFuture();
            });
            // 释放锁
            return Future.succeededFuture(true);
        });
    }

    public boolean isMatching(String username) {
        Player player = new Player(username);
        return waitingQueue.contains(player) || matchingQueue.contains(player);
    }

    /**
     * 取消匹配
     * 当玩家不在线时要取消匹配 {@link cn.spirit.go.web.socket.ClientManger}
     */
    public Future<Boolean> cancel(String username) {
        return AppContext.withLock(
                LockConstant.GAME_LOCK + username,
                1000,
                () -> Future.succeededFuture(waitingQueue.remove(new Player(username, 0)))
        );
    }

    private Future<Player> match(Player p, Integer range) {
        if (waitingQueue.isEmpty() || range + 200 >= MAX_RANGE) {
            return Future.succeededFuture(null);
        }
        for (Player wp : waitingQueue) {
            if (Math.abs(p.compareTo(wp)) <= range) {
                 return AppContext.withLock(LockConstant.ROOM_LOCK + wp.username, 200, () -> {
                     // 删除等待匹配队列加入到正在匹配的队列
                    if (waitingQueue.remove(wp)) {
                        matchingQueue.add(wp);
                        return Future.succeededFuture(wp);
                    }
                    return Future.succeededFuture(null);
                 }).compose(opponent -> {
                     // 要释放锁
                     if (null == opponent) {
                        return match(p, range + 200);
                     } else {
                         return Future.succeededFuture(opponent);
                     }
                 });
            }
        }
        return match(p, range + 200);
    }

    private static class Player implements Comparable<Player> {

        private final String username;

        private final Integer rating;

        private final Long time;

        public Player(String username, Integer rating) {
            this.username = username;
            this.rating = rating;
            this.time = System.currentTimeMillis();
        }

        private Player(String username) {
            this(username, 0);
        }

        @Override
        public int compareTo(Player o) {
            return this.rating - o.rating;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Player player = (Player) o;
            return Objects.equals(username, player.username);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(username);
        }
    }
}
