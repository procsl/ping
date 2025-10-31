package cn.procsl.ping.boot.mhpp.protocol;

import java.nio.charset.StandardCharsets;

public class PongPacket implements Packet {

    final byte[] body;

    public PongPacket(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
    }


    @Override
    public Packet.Command getCommand() {
        return Packet.Command.heartbeat;
    }

    @Override
    public long getChannelId() {
        return 0;
    }

    @Override
    public int getLength() {
        return body.length;
    }

    @Override
    public byte[] getBodyBytes() {
        return body;
    }

}
