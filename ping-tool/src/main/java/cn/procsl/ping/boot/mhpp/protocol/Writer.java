package cn.procsl.ping.boot.mhpp.protocol;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Getter
class Writer {

    private final Packet packet;
    private final long sequence;


    public Writer(Packet packet) {
        this.packet = packet;
        byte[] bytes = packet.getBodyBytes();
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, 8);
        buffer.order(ByteOrder.BIG_ENDIAN); // 或 LITTLE_ENDIAN
        this.sequence = buffer.getLong();
    }


}
