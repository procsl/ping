package cn.procsl.ping.boot.mhpp.protocol;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class Channel {

    private SocketConnector socketConnector;
    private final HashMap<Long, Writer> write = new HashMap<>();

    private final AtomicLong writeIndex = new AtomicLong(0);

    private boolean closed = false;

    private long channelId;

    public Channel() {
    }

    public Packet readFromSocket() {
        if (this.isClose()) {
            log.warn("连接被关闭");
            return null;
        }
        if (this.socketConnector == null) {
            return null;
        }

        byte[] b = new byte[2048];
        int t;
        try {
            t = this.socketConnector.readFromSocket(b, 0, 2048);
            return new TransferPacket(this.channelId, b, t);
        } catch (IOException e) {
            this.close(null);
        }
        return null;
    }

    // 刷入数据
    public Long writeToSocket() {
        do {

            if (this.isClose()) {
                log.warn("连接被关闭");
                return -1L;
            }

            Writer writer = this.write.get(writeIndex.get());
            if (writer == null) {
                // TODO 超过多次则关闭
                return -2L;
            }
            try {
                this.socketConnector.writeToSocket(writer.getPacket());
                this.write.remove(writer.getSequence());
                this.writeIndex.incrementAndGet();
            } catch (IOException e) {
                log.info("连接已经被关闭: {}", writer.getPacket().getChannelId());
                this.close(writer.getPacket());
                return -3L;
            }
            // 写入
            // 上报 TODO
        } while (!this.write.isEmpty());
        return this.writeIndex.get();
    }

    public boolean isClose() {
        return closed;
    }

    public void init(Packet packet) {
        if (socketConnector != null) {
            log.warn("已经被初始化: {}", packet.getChannelId());
            return;
        }
        this.socketConnector = new SocketConnector(packet);
        this.socketConnector.init();
        this.channelId = packet.getChannelId();
    }

    public void close(Packet packet) {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.socketConnector.close();
        this.write.clear();
    }

    public void transfer(Packet packet) {
        Writer writer = new Writer(packet);
        this.write.put(writer.getSequence(), writer);
    }
}
