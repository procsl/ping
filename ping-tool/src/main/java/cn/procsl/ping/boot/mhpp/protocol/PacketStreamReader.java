package cn.procsl.ping.boot.mhpp.protocol;

public interface PacketStreamReader<T> {



    T pull() throws InterruptedException;

}
