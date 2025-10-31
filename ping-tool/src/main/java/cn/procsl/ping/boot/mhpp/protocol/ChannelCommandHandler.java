package cn.procsl.ping.boot.mhpp.protocol;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ChannelCommandHandler implements CommandHandler {

    private final PacketStreamWriter<Packet> writer;
    private final ConcurrentHashMap<Long, Channel> channels = new ConcurrentHashMap<>();


    @Override
    public boolean handle(Packet packet) {
        // 由于可能会使用多来源, 故先设置,后初始化
        this.channels.computeIfAbsent(packet.getChannelId(), p -> this.channels.put(p, new Channel()));

        boolean t = switch (packet.getCommand()) {
            case Packet.Command.transfer -> {
                this.channels.get(packet.getChannelId()).transfer(packet);
                yield true;
            }
            case Packet.Command.create_channel -> {
                this.channels.get(packet.getChannelId()).init(packet);
                yield true;
            }
            case Packet.Command.close_channel -> {
                this.channels.get(packet.getChannelId()).close(packet);
                yield true;
            }
            default -> false;
        };

        // 清理关闭的channel, TODO
        this.channels.keySet().removeIf(i -> this.channels.get(i).isClose());
        return t;
    }

}
