package cn.spirit.go.web;

public class UserSession {

    /**
     * 会话ID
     */
    public String sId;

    /**
     * 用户id
     */
    public String uid;

    /**
     * 用户
     */
    public String username;

    /**
     * 登录IP
     */
    public String ip;

    /**
     * 是不是访客
     */
    public Boolean visitor = false;


    @Override
    public String toString() {
        return "UserSession{" +
                "sId='" + sId + '\'' +
                ", uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
