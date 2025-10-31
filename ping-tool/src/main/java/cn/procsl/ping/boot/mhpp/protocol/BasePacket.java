package cn.procsl.ping.boot.mhpp.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BasePacket implements Packet {


    // 通道id8字节,类型1个字节,长度2个字节
    byte[] controlBytes = new byte[11];

    int index = 0;

    // 数据
    byte[] dataBytes = null;

    int dataIndex = 0;

    private boolean parse(int i) {
        if (index < controlBytes.length) {
            controlBytes[index] = (byte) i;
            index++;
            return false;
        }

        if (index == controlBytes.length) {
            int len = this.getDataLength();
            if (len < 0 || len > 65500) {
                throw new IllegalArgumentException("数据长度非法,可能造成通道数据错乱");
            }

            // 校验是否类型错误
            try {
                this.getCommand();
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("指令类型错误", e);
            }

            dataBytes = new byte[len];
            index++;
            return false;
        }

        dataBytes[dataIndex] = (byte) i;
        dataIndex++;
        return dataBytes.length <= dataIndex;
    }


    private int getDataLength() {
        ByteBuffer buffer = ByteBuffer.wrap(this.controlBytes, 9, 2);
        buffer.order(ByteOrder.BIG_ENDIAN); // 或 LITTLE_ENDIAN
        return buffer.getInt();
    }

    // 数据包类型
    public Packet.Command getCommand() {
        return Packet.Command.parse(controlBytes[8]);
    }

    @Override
    public long getChannelId() {
        ByteBuffer buffer = ByteBuffer.wrap(this.controlBytes, 0, 8);
        buffer.order(ByteOrder.BIG_ENDIAN); // 或 LITTLE_ENDIAN
        return buffer.getLong();
    }

    @Override
    public int getLength() {
        return this.dataIndex;
    }

    @Override
    public byte[] getBodyBytes() {
        return this.dataBytes;
    }

    public static Packet parserPacket(InputStream inputStream) throws IOException {
        BasePacket tmp = new BasePacket();
        boolean isFinal;
        do {
            int bytesRead = inputStream.read();
            if (bytesRead <= -1) {
                throw new IOException("输入流已结束被关闭,picket解析未完成");
            }
            isFinal = tmp.parse(bytesRead);
        } while (!isFinal);

        return tmp;
    }


}
