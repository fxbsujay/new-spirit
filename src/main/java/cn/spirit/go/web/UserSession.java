package cn.spirit.go.web;

public class UserSession {

    public String sessionId;

    /**
     * 用户
     */
    public String username;

    /**
     * 分数
     */
    public Integer score;

    /**
     * 是否是游客
     */
    public Boolean isGuest;

    /**
     * 登录IP
     */
    public String ip;


    @Override
    public String toString() {
        return "UserSession{" +
                "sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
