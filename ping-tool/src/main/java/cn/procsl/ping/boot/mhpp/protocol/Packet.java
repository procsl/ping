package cn.procsl.ping.boot.mhpp.protocol;

import java.nio.ByteBuffer;

public interface Packet {

    enum Command {
        // 创建通道
        create_channel((byte) 1),
        // 关闭通道
        close_channel((byte) 2),
        // 心跳
        heartbeat((byte) 3),
        // 数据传输
        transfer((byte) 4);

        private final byte t;

        public byte getByte() {
            return t;
        }

        Command(byte t) {
            this.t = t;
        }

        public static Command parse(byte controlByte) {
            if (create_channel.t == controlByte) {
                return create_channel;
            }
            if (close_channel.t == controlByte) {
                return close_channel;
            }
            if (transfer.t == controlByte) {
                return transfer;
            }
            if (heartbeat.t == controlByte) {
                return heartbeat;
            }
            return null;
        }
    }

    Command getCommand();

    // 通道ID
    long getChannelId();

    // 数据包长度
    int getLength();

    // 数据包
    byte[] getBodyBytes();

    default byte[] createProtocolBytes() {
        ByteBuffer res = ByteBuffer.allocate(11 + this.getLength());
        byte[] buf = ByteBuffer.allocate(8).putLong(this.getChannelId()).array();
        byte[] len = ByteBuffer.allocate(2).putInt(this.getLength()).array();
        res.put(buf).put(this.getCommand().getByte()).put(len).put(this.getBodyBytes(), 0, this.getLength());
        return res.array();
    }

}
