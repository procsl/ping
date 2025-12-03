package cn.procsl.ping.boot.mhpp.protocol;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
class SimplePacketStream implements PacketStreamWriter<Packet>, PacketStreamReader<Packet> {


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
