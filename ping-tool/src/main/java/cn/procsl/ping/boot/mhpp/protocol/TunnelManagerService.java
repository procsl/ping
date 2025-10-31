package cn.procsl.ping.boot.mhpp.protocol;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class TunnelManagerService {

    private final List<CommandHandler> handlers = new ArrayList<>();
    private final SimplePacketStream up = new SimplePacketStream();
    private final SimplePacketStream down = new SimplePacketStream();
    private final TunnelWrapper tunnel = new TunnelWrapper();

    {
        handlers.add(new ChannelCommandHandler(up));
        handlers.add(new HeartbeatCommandHandler(up));
        tunnel.register("HTTP-TUNNEL", () -> new HttpTunnel("http://127.0.0.1:8849/mhpp/channels/1.0.0"));
    }


    // 处理下行的数据包
    @SneakyThrows
    protected void handlerDownStreamTask() {
        Packet e = this.down.pull();
        for (CommandHandler handler : this.handlers) {
            boolean res = handler.handle(e);
            if (res) {
                return;
            }
        }
        log.warn("无处理器处理该包: channel: {}, command: {}", e.getChannelId(), e.getCommand());
    }

    void run(Runnable runnable) {
        for (; ; ) {
            runnable.run();
        }
    }

    // 解析下载的数据包放入下行队列
    void parsePacketToDownStream() {
        try {
            Packet res = this.tunnel.read();
            this.down.push(res);
        } catch (Exception e) {
            log.error("解析数据失败,休眠后重试: ", e);
            // 休眠2000ms
            sleep();
        }
    }

    // 发送上行的数据包
    @SneakyThrows
    protected void pushToServer() {
        Packet last = null;
        try {
            log.info("开始推送数据至服务端");
            last = this.up.pull();
            this.tunnel.write(last);
            last = null;
            log.info("成功推送数据至服务端");
        } catch (IOException e) {
            // 休眠2000ms
            // 稍候重新发送
            log.warn("推送数据至服务端失败", e);
            this.up.push(last);
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            log.warn("休眠失败", ex);
        }
    }

    public void start() throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        executor.submit(() -> this.run(this::parsePacketToDownStream));
        executor.submit(() -> this.run(this::pushToServer));
        executor.submit(() -> this.run(this::handlerDownStreamTask));
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (CommandHandler handler : this.handlers) {
                handler.onPreStop();
            }
            latch.countDown();
        }));
        for (CommandHandler handler : this.handlers) {
            handler.onStarted();
        }
        latch.await();
        log.info("服务已关闭");
    }

    private static class SimplePacketStream implements PacketStreamWriter<Packet>, PacketStreamReader<Packet> {

        private final LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<>();

        @Override
        public Packet pull() throws InterruptedException {
            Packet temp = queue.take();
            log.info("取出队列元素: {}, {}", temp.getCommand(), temp.getChannelId());
            return temp;
        }

        @Override
        public void push(Packet temp) {
            log.info("添加队列元素: {}, {}", temp.getCommand(), temp.getChannelId());
            this.queue.add(temp);
        }
    }

}
