package cn.spirit.go.model;

public class RoomChatMessage {

    /**
     * 消息内容
     */
    public String content;

    /**
     * 发送者
     */
    public String sender;

    /**
     * 消息类型
     */
    public RoomChatMessageType type;

    /**
     * 发生时间
     */
    public Long timestamp;

    public enum RoomChatMessageType {
        CHAT, NOTICE
    }

}
