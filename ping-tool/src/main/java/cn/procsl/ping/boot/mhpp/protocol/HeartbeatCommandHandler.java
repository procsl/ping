package cn.procsl.ping.boot.mhpp.protocol;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class HeartbeatCommandHandler implements CommandHandler {

    private final PacketStreamWriter<Packet> writer;

    @Override
    public boolean handle(Packet packet) {
        if (packet.getCommand().equals(Packet.Command.heartbeat)) {
            this.writer.push(new PongPacket("响应心跳请求"));
            return true;
        }
        return false;
    }

    @Override
    public void onStarted() {
        log.info("发送注册请求");
        this.writer.push(new PongPacket("客户端服务注册"));
    }

}
