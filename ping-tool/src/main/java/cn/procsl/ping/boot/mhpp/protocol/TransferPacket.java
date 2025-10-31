package cn.procsl.ping.boot.mhpp.protocol;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransferPacket implements Packet {

    private final long channelId;
    private final byte[] bytes;
    private final int length;

    @Override
    public Command getCommand() {
        return Command.transfer;
    }

    @Override
    public long getChannelId() {
        return channelId;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public byte[] getBodyBytes() {
        return bytes;
    }
}
