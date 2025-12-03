package cn.procsl.ping.boot.mhpp.protocol;

public class ChannelManagerService {

    PacketStreamReader<Packet> forwarder = new SimplePacketStream();
    PacketStreamReader<Packet> receiver = new SimplePacketStream();

    /**
     * 接收数据
     */
    public void receive(Packet packet) {

    }

}
