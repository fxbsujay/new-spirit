package cn.spirit.go.web.socket;

public class SocketPackage {

    public PackageType type;

    public Object data;

    public Long timestamp = System.currentTimeMillis();

    public static SocketPackage build(PackageType type, Object data) {
        SocketPackage pack = new SocketPackage();
        pack.type = type;
        pack.data = data;
        return pack;
    }

    @Override
    public String toString() {
        return "SocketPackage{" +
                "type=" + type +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
