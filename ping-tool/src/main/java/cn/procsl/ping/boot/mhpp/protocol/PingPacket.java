package cn.procsl.ping.boot.mhpp.protocol;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PingPacket implements Packet {

    private final Packet body;

    @Override
    public Command getCommand() {
        return body.getCommand();
    }

    @Override
    public long getChannelId() {
        return body.getChannelId();
    }

    @Override
    public int getLength() {
        return body.getLength();
    }

    @Override
    public byte[] getBodyBytes() {
        return body.getBodyBytes();
    }

    public String toBody() {
        return new String(body.getBodyBytes(), 0, body.getLength());
    }
}
