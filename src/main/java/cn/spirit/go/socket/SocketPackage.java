package cn.spirit.go.socket;

public class SocketPackage<T> {

    public PackageType type;

    public T data;

    public String sender;

    public Long timestamp = System.currentTimeMillis();

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
