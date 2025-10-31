package cn.procsl.ping.boot.mhpp.protocol;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
@RequiredArgsConstructor
public class SocketConnector {

    @SneakyThrows
    public void close() {
        this.socket.close();
    }

    public int readFromSocket(byte[] b, int off, int len) throws IOException {
        return this.socket.getInputStream().read(b, off, len);
    }

    enum SocketType {
        UDP((byte) 0x01), TCP((byte) 0x02), UNKNOWN((byte) 0x00);
        final byte b;

        SocketType(byte b) {
            this.b = b;
        }


        public static SocketType parse(byte b) {
            if (b == UDP.b) {
                return UDP;
            }
            if (b == TCP.b) {
                return TCP;
            }
            throw new IllegalArgumentException("未知的协议类型: {}" + b);
        }
    }

    enum IPType {
        V4((byte) 0x01), V6((byte) 0x02);
        final byte b;

        IPType(byte b) {
            this.b = b;
        }

        public static IPType parse(byte b) {
            if (b == IPType.V4.b) {
                return IPType.V4;
            }
            if (b == IPType.V6.b) {
                return IPType.V6;
            }
            throw new IllegalArgumentException("未知的协议类型: {}" + b);
        }
    }

    //[协议类型:1字节,TCP|UDP][IP类型:1字节,v4|v6][目标端口:2字节][连接发起后超时时间毫秒数:1字节][通过目标主机所访问的ip]
    private final Packet packet;

    private SocketType socketType;

    private InetAddress inetAddress;

    private int port;

    private int timeout;

    private Socket socket;

    @SneakyThrows
    public void init() {
        byte[] bytes = packet.getBodyBytes();
        if (bytes.length < 8) {
            throw new IllegalArgumentException("初始化数据包长度不够");
        }
        this.socketType = SocketType.parse(bytes[0]);
        IPType ipType = IPType.parse(bytes[1]);
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 1, 2);
        buffer.order(ByteOrder.BIG_ENDIAN); // 或 LITTLE_ENDIAN
        this.port = buffer.getInt();
        this.timeout = bytes[4];
        if (ipType == IPType.V4) {
            ByteBuffer ip = ByteBuffer.wrap(bytes, 5, 4);
            inetAddress = Inet4Address.getByAddress(ip.array());
            return;
        }

        if (ipType == IPType.V6) {
            ByteBuffer ip = ByteBuffer.wrap(bytes, 5, 16);
            inetAddress = Inet6Address.getByAddress(ip.array());
        }
        log.info("初始化: socket type:{}, ip: {}, port: {}, timeout: {}", socketType, inetAddress, port, timeout);
    }

    public void writeToSocket(Packet packet) throws IOException {
        if (packet.getLength() <= 0) {
            return;
        }
        if (socket == null) {
            this.initSocket();
        }
        this.socket.getOutputStream().write(packet.getBodyBytes(), 8, packet.getLength());
    }

    private void initSocket() throws IOException {
        if (this.socketType == SocketType.TCP) {
            this.socket = new Socket();
            socket.connect(new InetSocketAddress(this.inetAddress, port), timeout);
            socket.setSoTimeout(timeout);
            socket.setReuseAddress(true);
        }
    }

}
