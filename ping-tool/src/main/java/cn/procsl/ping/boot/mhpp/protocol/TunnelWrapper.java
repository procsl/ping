package cn.procsl.ping.boot.mhpp.protocol;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

// 可重置的tunnel, 包装后的tunnel
@Slf4j
class TunnelWrapper implements Tunnel {

    private final Map<String, TunnelCreator> tunnelCreators = new HashMap<>();

    private final AtomicReference<Tunnel> ref = new AtomicReference<>();

    public void register(String name, TunnelCreator creator) {
        this.tunnelCreators.put(name, creator);
    }

    interface TunnelCreator {
        Tunnel get() throws IOException;
    }

    @Override
    public void joinToServer() {
        tunnelCreators.forEach((name, creator) -> {
            log.info("创建tunnel实例: [{}]", name);
            try {
                ref.set(creator.get());
            } catch (IOException e) {
                log.error("实例创建失败:{} ", name, e);
            }
        });
    }

    @Override
    public boolean isClosed() {
        return this.ref.get().isClosed();
    }

    @Override
    public void close() {
    }

    private Tunnel resetOrGet() {
        if (this.ref.get() == null) {
            this.joinToServer();
        }

        if (this.ref.get().isClosed()) {
            this.joinToServer();
        }
        return this.ref.get();
    }

    @Override
    public void write(Packet packet) throws Exception {
        log.info("写入数据至tunnel: {}, {}", packet.getCommand(), packet.getChannelId());
        this.resetOrGet().write(packet);
        log.info("写入数据至tunnel成功: {}, {}", packet.getCommand(), packet.getChannelId());
    }

    @Override
    public Packet read() throws Exception {
        log.info("读取数据");
        return this.resetOrGet().read();
    }
}
