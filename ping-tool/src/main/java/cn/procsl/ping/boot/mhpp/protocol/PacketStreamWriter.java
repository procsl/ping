package cn.procsl.ping.boot.mhpp.protocol;

public interface PacketStreamWriter<T> {


    void push(T temp);

}
