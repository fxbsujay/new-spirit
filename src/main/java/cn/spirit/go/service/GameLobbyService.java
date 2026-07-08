package cn.spirit.go.service;

import cn.spirit.go.model.CasualGameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏大厅-休闲自定义对局
 * 创建的游戏对局，等待对手加入游戏，游戏开始后删除游戏，用户连接断开后删除<br/>
 * 游戏未开始只存在内存中，不保存数据，游戏开始后保存数据
 */
public class GameLobbyService {

    private static final Logger log = LoggerFactory.getLogger(GameLobbyService.class);

    /**
     * uid -> code
     */
    private final Map<String, String> userGames = new HashMap<>();

    /**
     * code -> game
     */
    private final Map<String, CasualGameInfo> games = new HashMap<>();

    public List<CasualGameInfo> getGames() {
        return new ArrayList<>(games.values());
    }

    /**
     * 创建游戏
     * @param game      对局
     */
    public boolean addGame(CasualGameInfo game) {
        if (userGames.containsKey(game.uid)) {
            log.warn("{} failed to create the game", game.uid);
            return false;
        }
        game.timestamp = System.currentTimeMillis();
        userGames.put(game.uid, game.code);
        games.put(game.code, game);
        log.info("{} has created a game, code = {}", game.uid, game.code);
        return true;
    }

    /**
     * 删除自己的游戏
     * socket断开时删除、自己取消游戏时删除、别人加入游戏时删除
     *
     * @param uid  用户ID
     */
    public CasualGameInfo removeGame(String uid) {
        String code = userGames.remove(uid);
        if (null != code) {
            return games.remove(code);
        }
        return null;
    }

    public CasualGameInfo get(String code) {
        return games.get(code);
    }

    public CasualGameInfo getByUid(String uid) {
        String code = userGames.get(uid);
        if (null == code) {
            return null;
        }
        return get(code);
    }

}
