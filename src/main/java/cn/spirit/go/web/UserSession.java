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
     * 登录IP
     */
    public String ip;

    /**
     * 是不是访客
     */
    public Boolean isGuest = false;


    @Override
    public String toString() {
        return "UserSession{" +
                "sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", score=" + score +
                ", ip='" + ip + '\'' +
                '}';
    }
}
