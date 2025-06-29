package cn.spirit.go.socket;

public class SocketPackage<T> {

    public Integer type;

    public T data;

    public String sender;

    public Long timestamp;

    @Override
    public String toString() {
        return "SocketPackage{" +
                "type=" + type +
                ", data=" + data +
                ", sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
