package cn.spirit.go.web.socket;

public class SocketPackage<T> {

    public PackageType type;

    public T data;

    public String sender;

    public Long timestamp = System.currentTimeMillis();

    public static <T> SocketPackage<T> build(PackageType type, String sender, T data) {
        SocketPackage<T> pack = new SocketPackage<T>();
        pack.type = type;
        pack.sender = sender;
        pack.data = data;
        return pack;
    }


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
